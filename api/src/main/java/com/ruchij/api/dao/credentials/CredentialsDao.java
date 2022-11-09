package com.ruchij.api.dao.credentials;

import com.ruchij.api.dao.credentials.models.Credentials;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CredentialsDao {
    CompletableFuture<Credentials> insert(Credentials credentials);

    CompletableFuture<Optional<Credentials>> findByUserId(String userId);
}
