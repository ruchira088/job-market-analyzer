package com.ruchij.service.linkedin;

import com.ruchij.dao.elasticsearch.models.EncryptedText;
import com.ruchij.dao.linkedin.EncryptedLinkedInCredentialsDao;
import com.ruchij.dao.linkedin.models.EncryptedLinkedInCredentials;
import com.ruchij.exceptions.ResourceNotFoundException;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.encryption.EncryptionService;
import com.ruchij.service.linkedin.models.LinkedInCredentials;
import com.ruchij.utils.Transformers;
import io.reactivex.rxjava3.core.Flowable;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class LinkedInCredentialsServiceImpl implements LinkedInCredentialsService {
    private final EncryptedLinkedInCredentialsDao encryptedLinkedInCredentialsDao;
    private final EncryptionService encryptionService;
    private final Clock clock;

    public LinkedInCredentialsServiceImpl(
        EncryptedLinkedInCredentialsDao encryptedLinkedInCredentialsDao,
        EncryptionService encryptionService,
        Clock clock
    ) {
        this.encryptedLinkedInCredentialsDao = encryptedLinkedInCredentialsDao;
        this.encryptionService = encryptionService;
        this.clock = clock;
    }

    @Override
    public CompletableFuture<LinkedInCredentials> getByUserId(String userId) {
        return encryptedLinkedInCredentialsDao.findByUserId(userId)
            .thenCompose(maybeCredentials ->
                Transformers.convert(
                    maybeCredentials,
                    () -> new ResourceNotFoundException("LinkedIn credentials not found for userId=%s".formatted(userId))
                )
            )
            .thenCompose(encryptedLinkedInCredentials -> Transformers.lift(() -> decrypt(encryptedLinkedInCredentials)));
    }

    @Override
    public Flowable<LinkedInCredentials> getAll() {
        return encryptedLinkedInCredentialsDao.getAll()
            .map(this::decrypt);
    }

    @Override
    public CompletableFuture<LinkedInCredentials> deleteByUserId(String userId) {
        return getByUserId(userId)
            .thenCompose(linkedInCredentials ->
                encryptedLinkedInCredentialsDao.deleteByUserId(userId).thenApply(__ -> linkedInCredentials)
            );
    }

    @Override
    public CompletableFuture<LinkedInCredentials> insert(String userId, String email, String password) {
        try {
            Instant timestamp = clock.timestamp();

            String encryptedEmail = encryptionService.encrypt(email.getBytes());
            String encryptedPassword = encryptionService.encrypt(password.getBytes());

            EncryptedLinkedInCredentials encryptedLinkedInCredentials =
                new EncryptedLinkedInCredentials(
                    userId,
                    timestamp,
                    new EncryptedText(encryptedEmail),
                    new EncryptedText(encryptedPassword)
                );

            return encryptedLinkedInCredentialsDao.insert(encryptedLinkedInCredentials)
                .thenApply(__ -> new LinkedInCredentials(userId, email, password));
        } catch (GeneralSecurityException generalSecurityException) {
            return CompletableFuture.failedFuture(generalSecurityException);
        }
    }

    private LinkedInCredentials decrypt(EncryptedLinkedInCredentials encryptedLinkedInCredentials)
        throws GeneralSecurityException {
        String email = decrypt(encryptedLinkedInCredentials.email());
        String password = decrypt(encryptedLinkedInCredentials.password());

        return new LinkedInCredentials(encryptedLinkedInCredentials.userId(), email, password);
    }

    private String decrypt(EncryptedText encryptedText) throws GeneralSecurityException {
        byte[] decryptedBytes = encryptionService.decrypt(encryptedText.value());
        return new String(decryptedBytes);
    }
}
