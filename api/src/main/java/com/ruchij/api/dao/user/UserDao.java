package com.ruchij.api.dao.user;

import com.ruchij.api.dao.user.models.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserDao {
	CompletableFuture<String> insert(User user);

	CompletableFuture<Optional<User>> findById(String userId);

	CompletableFuture<Optional<User>> findByEmail(String email);
}
