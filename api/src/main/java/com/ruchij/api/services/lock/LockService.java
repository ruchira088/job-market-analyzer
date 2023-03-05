package com.ruchij.api.services.lock;

import com.ruchij.api.services.lock.models.Lock;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LockService {
	CompletableFuture<Optional<Lock>> lock(String lockId, Duration timeout);

	CompletableFuture<Boolean> release(String lockId);
}
