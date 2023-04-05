package com.ruchij.api.dao.credentials;

import com.ruchij.api.dao.credentials.models.Credentials;
import com.ruchij.crawler.utils.Kleisli;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CredentialsDao<A> {
	Kleisli<A, String> insert(Credentials credentials);

	Kleisli<A, Optional<Credentials>> findByUserId(String userId);
}
