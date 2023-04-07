package com.ruchij.crawler.dao.linkedin;

import com.ruchij.crawler.dao.jdbi.models.JdbiEncryptedLinkedInCredentials;
import com.ruchij.crawler.dao.linkedin.models.EncryptedLinkedInCredentials;
import com.ruchij.crawler.utils.Kleisli;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbiEncryptedLinkedInCredentialsDao implements EncryptedLinkedInCredentialsDao<Handle> {
	private static final String SQL_INSERT =
		"""
			  INSERT INTO linkedin_credentials(user_id, created_at, encrypted_email, encrypted_password) 
			    VALUES(:userId, :createdAt, :encryptedEmail, :encryptedPassword)
			""";

	private static final String SQL_SELECT =
		"SELECT user_id, created_at, encrypted_email, encrypted_password FROM linkedin_credentials";

	@Override
	public Kleisli<Handle, String> insert(EncryptedLinkedInCredentials encryptedLinkedInCredentials) {
		return new Kleisli<Handle, Integer>(handle ->
			CompletableFuture.completedFuture(
				handle
					.createUpdate(SQL_INSERT)
					.bindBean(JdbiEncryptedLinkedInCredentials.from(encryptedLinkedInCredentials))
					.execute()
			)
		)
			.map(__ -> encryptedLinkedInCredentials.userId());
	}

	@Override
	public Kleisli<Handle, List<EncryptedLinkedInCredentials>> getAll(int pageNumber, int pageSize) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s LIMIT :limit OFFSET :offset".formatted(SQL_SELECT))
					.bind("limit", pageSize)
					.bind("offset", pageNumber * pageSize)
					.mapToBean(JdbiEncryptedLinkedInCredentials.class)
					.map(JdbiEncryptedLinkedInCredentials::encryptedLinkedInCredentials)
					.list()
			)
		);
	}

	@Override
	public Kleisli<Handle, Optional<EncryptedLinkedInCredentials>> findByUserId(String userId) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE user_id = :userId".formatted(SQL_SELECT))
					.bind("userId", userId)
					.mapToBean(JdbiEncryptedLinkedInCredentials.class)
					.findOne()
					.map(JdbiEncryptedLinkedInCredentials::encryptedLinkedInCredentials)
			)
		);
	}

	@Override
	public Kleisli<Handle, Boolean> deleteByUserId(String userId) {
		return new Kleisli<Handle, Integer>(handle ->
			CompletableFuture.completedFuture(
				handle.createUpdate("DELETE FROM linkedin_credentials WHERE user_id = :userId")
					.bind("userId", userId)
					.execute()
			)
		)
			.map(count -> count > 0);
	}
}
