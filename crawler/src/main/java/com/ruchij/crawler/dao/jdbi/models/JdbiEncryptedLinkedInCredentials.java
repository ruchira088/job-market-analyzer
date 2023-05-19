package com.ruchij.crawler.dao.jdbi.models;

import com.ruchij.crawler.dao.elasticsearch.models.EncryptedText;
import com.ruchij.crawler.dao.linkedin.models.EncryptedLinkedInCredentials;

import java.time.Instant;

public class JdbiEncryptedLinkedInCredentials {
	private String userId;
	private Instant createdAt;
	private EncryptedText encryptedEmail;
	private EncryptedText encryptedPassword;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public EncryptedText getEncryptedEmail() {
		return encryptedEmail;
	}

	public void setEncryptedEmail(EncryptedText encryptedEmail) {
		this.encryptedEmail = encryptedEmail;
	}

	public EncryptedText getEncryptedPassword() {
		return encryptedPassword;
	}

	public void setEncryptedPassword(EncryptedText encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}

	public EncryptedLinkedInCredentials encryptedLinkedInCredentials() {
		return new EncryptedLinkedInCredentials(userId, createdAt, encryptedEmail, encryptedPassword);
	}

	public static JdbiEncryptedLinkedInCredentials from(EncryptedLinkedInCredentials encryptedLinkedInCredentials) {
		JdbiEncryptedLinkedInCredentials jdbiEncryptedLinkedInCredentials = new JdbiEncryptedLinkedInCredentials();
		jdbiEncryptedLinkedInCredentials.setUserId(encryptedLinkedInCredentials.userId());
		jdbiEncryptedLinkedInCredentials.setCreatedAt(encryptedLinkedInCredentials.createdAt());
		jdbiEncryptedLinkedInCredentials.setEncryptedEmail(encryptedLinkedInCredentials.email());
		jdbiEncryptedLinkedInCredentials.setEncryptedPassword(encryptedLinkedInCredentials.password());

		return jdbiEncryptedLinkedInCredentials;
	}
}
