package com.ruchij.dao.linkedin;


import com.ruchij.dao.linkedin.models.LinkedInCredentials;
import io.reactivex.rxjava3.core.Flowable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LinkedInCredentialsDao {
    CompletableFuture<String> insert(LinkedInCredentials linkedInCredentials);

    Flowable<LinkedInCredentials> getAll();

    CompletableFuture<Optional<LinkedInCredentials>> findByUserId(String userId);
}
