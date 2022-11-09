package com.ruchij.service.linkedin;

import com.ruchij.dao.elasticsearch.models.EncryptedText;
import com.ruchij.dao.linkedin.EncryptedLinkedInCredentialsDao;
import com.ruchij.dao.linkedin.models.EncryptedLinkedInCredentials;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.encryption.EncryptionService;
import com.ruchij.service.linkedin.models.LinkedInCredentials;
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
    public Flowable<LinkedInCredentials> getAll() {
        return encryptedLinkedInCredentialsDao.getAll()
            .map(encryptedLinkedInCredentials -> {
                String email = decrypt(encryptedLinkedInCredentials.getEmail());
                String password = decrypt(encryptedLinkedInCredentials.getPassword());

                return new LinkedInCredentials(encryptedLinkedInCredentials.getUserId(), email, password);
            });
    }

    @Override
    public CompletableFuture<String> insert(String userId, String email, String password) {
        try {
            Instant timestamp = clock.timestamp();

            String encryptedEmail = encryptionService.encrypt(email.getBytes());
            String encryptedPassword = encryptionService.encrypt(password.getBytes());

            EncryptedLinkedInCredentials encryptedLinkedInCredentials = new EncryptedLinkedInCredentials();
            encryptedLinkedInCredentials.setUserId(userId);
            encryptedLinkedInCredentials.setCreatedAt(timestamp);
            encryptedLinkedInCredentials.setEmail(new EncryptedText(encryptedEmail));
            encryptedLinkedInCredentials.setPassword(new EncryptedText(encryptedPassword));

            return encryptedLinkedInCredentialsDao.insert(encryptedLinkedInCredentials);
        } catch (GeneralSecurityException generalSecurityException) {
            return CompletableFuture.failedFuture(generalSecurityException);
        }
    }

    private String decrypt(EncryptedText encryptedText) throws GeneralSecurityException {
        byte[] decryptedBytes = encryptionService.decrypt(encryptedText.value());
        return new String(decryptedBytes);
    }
}
