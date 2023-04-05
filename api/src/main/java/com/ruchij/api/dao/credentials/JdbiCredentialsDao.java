package com.ruchij.api.dao.credentials;

import com.ruchij.api.dao.credentials.models.Credentials;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbiCredentialsDao implements CredentialsDao {
	private static final String SQL_INSERT =
		"INSERT INTO api_user_credentials(user_id, hashed_password) VALUES (:userId, :hashedPassword)";

	private static final String SQL_SELECT = "SELECT user_id, hashed_password FROM api_user_credentials";

	private final Jdbi jdbi;

	public JdbiCredentialsDao(Jdbi jdbi) {
		this.jdbi = jdbi;
	}

	@Override
	public CompletableFuture<String> insert(Credentials credentials) {

		jdbi.useHandle(handle ->
			handle.createUpdate(SQL_INSERT).bindBean(JdbiCredentials.from(credentials)).execute());

		return CompletableFuture.completedFuture(credentials.userId());
	}

	@Override
	public CompletableFuture<Optional<Credentials>> findByUserId(String userId) {
		Optional<Credentials> maybeCredentials = jdbi.withHandle(handle ->
			handle.createQuery("%s WHERE user_id = :id".formatted(SQL_SELECT))
				.bind("id", userId)
				.mapToBean(JdbiCredentials.class)
				.findOne()
				.map(JdbiCredentials::credentials)
		);

		return CompletableFuture.completedFuture(maybeCredentials);
	}

	public static class JdbiCredentials {
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
}
