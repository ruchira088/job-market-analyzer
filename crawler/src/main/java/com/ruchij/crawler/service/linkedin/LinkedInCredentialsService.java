package com.ruchij.crawler.service.linkedin;

import com.ruchij.crawler.service.linkedin.models.LinkedInCredentials;
import io.reactivex.rxjava3.core.Flowable;

import java.util.concurrent.CompletableFuture;

public interface LinkedInCredentialsService {
	Flowable<LinkedInCredentials> getAll();

	CompletableFuture<LinkedInCredentials> getByUserId(String userId);

	CompletableFuture<LinkedInCredentials> deleteByUserId(String userId);

	CompletableFuture<LinkedInCredentials> insert(String userId, String email, String password);
}
