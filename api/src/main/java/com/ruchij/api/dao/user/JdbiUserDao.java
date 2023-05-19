package com.ruchij.api.dao.user;

import com.ruchij.api.dao.jdbi.models.JdbiUser;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.crawler.utils.Kleisli;
import org.jdbi.v3.core.Handle;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbiUserDao implements UserDao<Handle> {
	private static final String SQL_INSERT =
		"""
			  INSERT INTO api_user(id, created_at, email, first_name, last_name) 
			    VALUES(:id, :createdAt, :email, :firstName, :lastName)
			""";

	private static final String SQL_SELECT = "SELECT id, created_at, email, first_name, last_name FROM api_user";

	@Override
	public Kleisli<Handle, String> insert(User user) {
		return new Kleisli<Handle, Integer>(handle ->
			CompletableFuture.completedFuture(handle.createUpdate(SQL_INSERT).bindBean(JdbiUser.from(user)).execute())
		).as(user.id());
	}

	@Override
	public Kleisli<Handle, Optional<User>> findById(String userId) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE id = :id".formatted(SQL_SELECT))
					.bind("id", userId)
					.mapToBean(JdbiUser.class)
					.findOne()
					.map(JdbiUser::user)
			)
		);
	}

	@Override
	public Kleisli<Handle, Optional<User>> findByEmail(String email) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE email = :email".formatted(SQL_SELECT))
					.bind("email", email)
					.mapToBean(JdbiUser.class)
					.findOne()
					.map(JdbiUser::user)
			)
		);
	}
}
