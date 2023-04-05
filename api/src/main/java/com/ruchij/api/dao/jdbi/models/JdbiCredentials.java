package com.ruchij.api.dao.jdbi.models;

import com.ruchij.api.dao.credentials.models.Credentials;

public class JdbiCredentials {
	private String userId;
	private String hashedPassword;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public Credentials credentials() {
		return new Credentials(userId, hashedPassword);
	}

	public static JdbiCredentials from(Credentials credentials) {
		JdbiCredentials jdbiCredentials = new JdbiCredentials();
		jdbiCredentials.setUserId(credentials.userId());
		jdbiCredentials.setHashedPassword(credentials.hashedPassword());

		return jdbiCredentials;
	}
}