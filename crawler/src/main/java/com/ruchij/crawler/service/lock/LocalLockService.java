package com.ruchij.crawler.service.lock;

import com.ruchij.crawler.service.clock.Clock;
import com.ruchij.crawler.service.lock.models.Lock;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocalLockService implements LockService {
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService;
    private final Clock clock;

    public LocalLockService(ScheduledExecutorService scheduledExecutorService, Clock clock) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.clock = clock;
    }

    @Override
    public synchronized CompletableFuture<Optional<Lock>> lock(String lockId, Duration timeout) {
        if (locks.containsKey(lockId)) {
            return CompletableFuture.completedFuture(Optional.empty());
        } else {
            Instant timestamp = clock.timestamp();
            Lock lock = new Lock(lockId, timestamp, timestamp.plus(timeout));
            locks.put(lockId, lock);

            scheduledExecutorService.schedule(
                () -> release(lock),
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
            );

            return CompletableFuture.completedFuture(Optional.of(lock));
        }
    }

    @Override
    public CompletableFuture<Boolean> release(Lock lock) {
        return CompletableFuture.completedFuture(locks.remove(lock.id()) != null);
    }
}
