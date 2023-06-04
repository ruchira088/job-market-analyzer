package com.ruchij.api.services.lock;

import com.ruchij.api.services.lock.models.Lock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InMemoryLockService implements LockService {
	private final Map<String, Lock> locks = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduledExecutorService;
	private final Clock clock;

	public InMemoryLockService(ScheduledExecutorService scheduledExecutorService, Clock clock) {
		this.scheduledExecutorService = scheduledExecutorService;
		this.clock = clock;
	}

	@Override
	public synchronized CompletableFuture<Optional<Lock>> lock(String lockId, Duration timeout) {
		if (locks.containsKey(lockId)) {
			return CompletableFuture.completedFuture(Optional.empty());
		} else {
			Instant timestamp = clock.instant();
			Lock lock = new Lock(lockId, timestamp, timestamp.plus(timeout));
			locks.put(lockId, lock);

			scheduledExecutorService.schedule(
				() -> release(lockId),
				timeout.toMillis(),
				TimeUnit.MILLISECONDS
			);

			return CompletableFuture.completedFuture(Optional.of(lock));
		}
	}

	@Override
	public CompletableFuture<Boolean> release(String lockId) {
		return CompletableFuture.completedFuture(locks.remove(lockId) != null);
	}
}
