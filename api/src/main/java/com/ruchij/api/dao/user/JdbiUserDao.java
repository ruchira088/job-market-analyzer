package com.ruchij.api.dao.user;

import com.ruchij.api.dao.jdbi.models.JdbiUser;
import com.ruchij.api.dao.user.models.User;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbiUserDao implements UserDao {
	private static final String SQL_INSERT =
		"""
			  INSERT INTO api_user(id, created_at, email, first_name, last_name) 
			    VALUES(:id, :createdAt, :email, :firstName, :lastName)
			""";

	private static final String SQL_SELECT = "SELECT id, created_at, email, first_name, last_name FROM api_user";

	private final Jdbi jdbi;

	public JdbiUserDao(Jdbi jdbi) {
		this.jdbi = jdbi;
	}

	@Override
	public CompletableFuture<String> insert(User user) {
		jdbi.useHandle(handle ->
			handle.createUpdate(SQL_INSERT).bindBean(JdbiUser.from(user)).execute()
		);

		return CompletableFuture.completedFuture(user.id());
	}

	@Override
	public CompletableFuture<Optional<User>> findById(String userId) {
		Optional<User> maybeUser = jdbi.withHandle(handle ->
			handle.createQuery("%s WHERE id = :id".formatted(SQL_SELECT))
				.bind("id", userId)
				.mapToBean(JdbiUser.class)
				.findOne()
				.map(JdbiUser::user)
		);

		return CompletableFuture.completedFuture(maybeUser);
	}

	@Override
	public CompletableFuture<Optional<User>> findByEmail(String email) {
		Optional<User> maybeUser = jdbi.withHandle(handle ->
			handle.createQuery("%s WHERE email = :email".formatted(SQL_SELECT))
				.bind("email", email)
				.mapToBean(JdbiUser.class)
				.findOne()
				.map(JdbiUser::user)
		);

		return CompletableFuture.completedFuture(maybeUser);
	}
}
