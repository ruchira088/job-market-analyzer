package com.ruchij.api.services.lock;

import com.ruchij.api.dao.lock.JdbiLockDao;
import com.ruchij.api.services.lock.models.Lock;
import com.ruchij.crawler.containers.JdbiContainer;
import com.ruchij.crawler.dao.transaction.JdbiTransactor;
import com.ruchij.test.TestClock;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Testcontainers
class DatabaseLockServiceTest {
	@Container
	private final JdbiContainer jdbiContainer = new JdbiContainer();

	private TestClock testClock;
	private DatabaseLockService<Handle> databaseLockService;

	@BeforeEach
	void beforeEach() {
		JdbiTransactor jdbiTransactor = jdbiContainer.jdbiTransactor();
		JdbiLockDao jdbiLockDao = new JdbiLockDao();

		testClock = new TestClock();
		Instant timestamp = Instant.parse("2023-06-04T07:55:58.585Z");
		testClock.setInstant(timestamp);

		databaseLockService = new DatabaseLockService<>(jdbiLockDao, jdbiTransactor, testClock);
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void shouldAcquireAndReleaseLock() throws Exception {
		String lockId = "my-lock-id";

		CompletableFuture<Optional<Lock>> optionalCompletableFutureOne =
			databaseLockService.lock(lockId, Duration.ofSeconds(20));

		Assertions.assertTrue(optionalCompletableFutureOne.get().isPresent());

		CompletableFuture<Optional<Lock>> optionalCompletableFutureTwo =
			databaseLockService.lock(lockId, Duration.ofSeconds(10));

		Assertions.assertTrue(optionalCompletableFutureTwo.get().isEmpty());

		databaseLockService.release(lockId).get();

		CompletableFuture<Optional<Lock>> optionalCompletableFutureThree =
			databaseLockService.lock(lockId, Duration.ofSeconds(20));

		Assertions.assertTrue(optionalCompletableFutureThree.get().isPresent());
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void concurrentlyAcquiringLocks() throws Exception {
		String lockId = "my-lock-id";
		ArrayList<CompletableFuture<Optional<Lock>>> completableFutures = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			CompletableFuture<Optional<Lock>> optionalCompletableFuture =
				databaseLockService.lock(lockId, Duration.ofSeconds(20));

			completableFutures.add(optionalCompletableFuture);
		}

		int lockCount = 0;

		for (CompletableFuture<Optional<Lock>> completableFuture :  completableFutures) {
			Optional<Lock> optionalLock = completableFuture.get();

			if (optionalLock.isPresent()) {
				lockCount++;
			}
		}

		Assertions.assertEquals(1, lockCount);
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void shouldAcquireExpiredLock() throws Exception {
		String lockId = "my-lock-id";

		CompletableFuture<Optional<Lock>> optionalCompletableFutureOne =
			databaseLockService.lock(lockId, Duration.ofSeconds(20));

		Assertions.assertTrue(optionalCompletableFutureOne.get().isPresent());

		testClock.setInstant(testClock.instant().plusSeconds(30));

		CompletableFuture<Optional<Lock>> optionalCompletableFutureTwo =
			databaseLockService.lock(lockId, Duration.ofSeconds(10));

		Assertions.assertTrue(optionalCompletableFutureTwo.get().isPresent());
	}
}