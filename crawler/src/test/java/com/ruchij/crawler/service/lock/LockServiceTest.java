package com.ruchij.crawler.service.lock;

import com.ruchij.crawler.service.clock.Clock;
import com.ruchij.crawler.service.lock.models.Lock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LockServiceTest {
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
    private final Clock clock = Clock.systemClock();

    private final LockService lockService = new LocalLockService(scheduledExecutorService, clock);

    @Test
    void lockContention() throws Exception {
        String lockId = UUID.randomUUID().toString();
        Duration timeout = Duration.ofMillis(5_000);
        ArrayList<ScheduledFuture<Optional<Lock>>> scheduledFutures = new ArrayList<>();

        for (int i = 0; i < 10_000; i++) {
            ScheduledFuture<Optional<Lock>> scheduledLock = scheduledExecutorService.schedule(
                () -> lockService.lock(lockId, timeout).get(100, TimeUnit.MILLISECONDS),
                100 - (i % 10),
                TimeUnit.MILLISECONDS
            );

            scheduledFutures.add(scheduledLock);
        }

        Assertions.assertEquals(10_000, scheduledFutures.size());

        int lockCount = 0;
        int noLockCount = 0;

        for (ScheduledFuture<Optional<Lock>> scheduledFuture : scheduledFutures) {
            if (scheduledFuture.get(1000, TimeUnit.MILLISECONDS).isPresent()) {
                lockCount++;
            } else {
                noLockCount++;
            }
        }

        Assertions.assertEquals(1, lockCount);
        Assertions.assertEquals(9999, noLockCount);
    }

    @Test
    void existingLock() throws Exception {
        String lockId = UUID.randomUUID().toString();
        Duration timeout = Duration.ofMillis(5_000);

        Optional<Lock> optionalLock = lockService.lock(lockId, timeout)
            .get(100, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(optionalLock.isPresent());

        for (int i = 0; i < 100; i++) {
            Assertions.assertFalse(
                lockService.lock(lockId, timeout)
                    .get(100, TimeUnit.MILLISECONDS)
                    .isPresent()
            );
        }

        lockService.release(optionalLock.get());

        Assertions.assertTrue(
            lockService.lock(lockId, timeout)
                .get(100, TimeUnit.MILLISECONDS)
                .isPresent()
        );
    }

    @Test
    void lockTimeouts() throws Exception {
        Instant instant = clock.timestamp();
        String lockId = UUID.randomUUID().toString();
        Duration timeout = Duration.ofMillis(500);

        Optional<Lock> optionalLock = lockService.lock(lockId, timeout)
            .get(100, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(optionalLock.isPresent());

        while (clock.timestamp().isBefore(instant.plus(timeout))) {
            Assertions.assertFalse(
                lockService.lock(lockId, timeout)
                    .get(100, TimeUnit.MILLISECONDS)
                    .isPresent()
            );
        }

        Thread.sleep(timeout.toMillis());

        Assertions.assertTrue(
            lockService.lock(lockId, timeout)
                .get(100, TimeUnit.MILLISECONDS)
                .isPresent()
        );
    }

    @AfterAll
    void afterAll() {
        scheduledExecutorService.shutdown();
    }

}