package com.ruchij.api.dao.credentials;

import com.ruchij.api.dao.credentials.models.Credentials;
import com.ruchij.api.dao.jdbi.models.JdbiCredentials;
import com.ruchij.crawler.utils.Kleisli;
import org.jdbi.v3.core.Handle;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbiCredentialsDao implements CredentialsDao<Handle> {
	private static final String SQL_INSERT =
		"INSERT INTO api_user_credentials(user_id, hashed_password) VALUES (:userId, :hashedPassword)";

	private static final String SQL_SELECT = "SELECT user_id, hashed_password FROM api_user_credentials";

	@Override
	public Kleisli<Handle, String> insert(Credentials credentials) {
		return new Kleisli<Handle, Integer>(handle ->
			CompletableFuture.completedFuture(
				handle.createUpdate(SQL_INSERT)
					.bindBean(JdbiCredentials.from(credentials))
					.execute()
			)).as(credentials.userId());
	}

	@Override
	public Kleisli<Handle, Optional<Credentials>> findByUserId(String userId) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE user_id = :id".formatted(SQL_SELECT))
					.bind("id", userId)
					.mapToBean(JdbiCredentials.class)
					.findOne()
					.map(JdbiCredentials::credentials)
			)
		);
	}
}
