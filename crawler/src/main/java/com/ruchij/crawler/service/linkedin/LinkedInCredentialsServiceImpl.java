package com.ruchij.crawler.service.linkedin;

import com.ruchij.crawler.dao.elasticsearch.models.EncryptedText;
import com.ruchij.crawler.dao.linkedin.EncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.linkedin.models.EncryptedLinkedInCredentials;
import com.ruchij.crawler.dao.transaction.Transactor;
import com.ruchij.crawler.exceptions.ResourceNotFoundException;
import com.ruchij.crawler.service.crawler.Crawler;
import com.ruchij.crawler.service.encryption.EncryptionService;
import com.ruchij.crawler.service.linkedin.models.LinkedInCredentials;
import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.Transformers;
import io.reactivex.rxjava3.core.Flowable;

import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class LinkedInCredentialsServiceImpl<A> implements LinkedInCredentialsService {
	private final EncryptedLinkedInCredentialsDao<A> encryptedLinkedInCredentialsDao;
	private final Transactor<A> transactor;
	private final Crawler crawler;
	private final EncryptionService encryptionService;
	private final Clock clock;

	public LinkedInCredentialsServiceImpl(
		EncryptedLinkedInCredentialsDao<A> encryptedLinkedInCredentialsDao,
		Transactor<A> transactor,
		Crawler crawler,
		EncryptionService encryptionService,
		Clock clock
	) {
		this.encryptedLinkedInCredentialsDao = encryptedLinkedInCredentialsDao;
		this.transactor = transactor;
		this.crawler = crawler;
		this.encryptionService = encryptionService;
		this.clock = clock;
	}

	@Override
	public CompletableFuture<LinkedInCredentials> getByUserId(String userId) {
		return transactor.transaction(getLinkedInCredentialsByUserId(userId));
	}

	private Kleisli<A, LinkedInCredentials> getLinkedInCredentialsByUserId(String userId) {
		return encryptedLinkedInCredentialsDao.findByUserId(userId)
			.flatMap(maybeCredentials ->
				new Kleisli<>(__ ->
					Transformers.convert(
						maybeCredentials,
						() -> new ResourceNotFoundException("LinkedIn credentials not found for userId=%s".formatted(userId))
					)
				)
			)
			.flatMap(encryptedLinkedInCredentials ->
				new Kleisli<>(__ -> Transformers.lift(() -> decrypt(encryptedLinkedInCredentials)))
			);
	}

	@Override
	public Flowable<LinkedInCredentials> getAll() {
		return transactor.transaction(encryptedLinkedInCredentialsDao.getAll())
			.map(this::decrypt);
	}

	@Override
	public CompletableFuture<LinkedInCredentials> deleteByUserId(String userId) {
		return transactor.transaction(
			getLinkedInCredentialsByUserId(userId)
				.flatMap(linkedInCredentials ->
					encryptedLinkedInCredentialsDao.deleteByUserId(userId).map(__ -> linkedInCredentials)
				)
		);
	}

	@Override
	public CompletableFuture<LinkedInCredentials> insert(String userId, String email, String password) {
		try {
			Instant timestamp = clock.instant();

			String encryptedEmail = encryptionService.encrypt(email.getBytes());
			String encryptedPassword = encryptionService.encrypt(password.getBytes());

			EncryptedLinkedInCredentials encryptedLinkedInCredentials =
				new EncryptedLinkedInCredentials(
					userId,
					timestamp,
					new EncryptedText(encryptedEmail),
					new EncryptedText(encryptedPassword)
				);

			return transactor.transaction(encryptedLinkedInCredentialsDao
				.insert(encryptedLinkedInCredentials).map(__ -> new LinkedInCredentials(userId, email, password))
			);
		} catch (GeneralSecurityException generalSecurityException) {
			return CompletableFuture.failedFuture(generalSecurityException);
		}
	}

	@Override
	public CompletableFuture<Boolean> verifyCredentials(String email, String password) {
		return this.crawler.verifyCredentials(email, password);
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
