package com.ruchij.api.dao.user;

import com.ruchij.api.dao.user.models.User;
import com.ruchij.crawler.utils.Kleisli;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserDao<A> {
	Kleisli<A, String> insert(User user);

	Kleisli<A, Optional<User>> findById(String userId);

	Kleisli<A, Optional<User>> findByEmail(String email);
}
