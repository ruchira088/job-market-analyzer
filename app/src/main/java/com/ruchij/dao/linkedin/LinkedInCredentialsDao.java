package com.ruchij.dao.linkedin;

import com.ruchij.config.LinkedInCredentials;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LinkedInCredentialsDao {
    CompletableFuture<String> insert(LinkedInCredentials linkedInCredentials);

    CompletableFuture<Optional<LinkedInCredentials>> findByUserId(String userId);
}
