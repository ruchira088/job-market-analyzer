package com.ruchij.service.linkedin;

import com.ruchij.service.linkedin.models.LinkedInCredentials;
import io.reactivex.rxjava3.core.Flowable;

import java.util.concurrent.CompletableFuture;

public interface LinkedInCredentialsService {
    Flowable<LinkedInCredentials> getAll();

    CompletableFuture<String> insert(String userId, String email, String password);
}
