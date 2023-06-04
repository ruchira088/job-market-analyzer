package com.ruchij.api.services.lock;

import com.ruchij.api.dao.lock.LockDao;
import com.ruchij.api.dao.lock.models.DatabaseLock;
import com.ruchij.api.services.lock.models.Lock;
import com.ruchij.crawler.dao.transaction.Transactor;
import com.ruchij.crawler.utils.Kleisli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DatabaseLockService<A> implements LockService {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseLockService.class);

	private final LockDao<A> lockDao;
	private final Transactor<A> transactor;
	private final Clock clock;

	public DatabaseLockService(LockDao<A> lockDao, Transactor<A> transactor, Clock clock) {
		this.lockDao = lockDao;
		this.transactor = transactor;
		this.clock = clock;
	}

	@Override
	public CompletableFuture<Optional<Lock>> lock(String lockId, Duration timeout) {
		Instant instant = clock.instant();
		Instant expiresAt = instant.plus(timeout);

		DatabaseLock databaseLock =
			new DatabaseLock(lockId, instant, expiresAt);

		Kleisli<A, Optional<Lock>> acquireLock = lockDao.getById(lockId)
			.flatMap(optionalDbLock ->
				optionalDbLock
					.map(dbLock -> {
						if (dbLock.getExpiresAt().isBefore(instant)) {
							return lockDao.deleteById(lockId).as(true);
						} else {
							logger.warn("Lock is active for lockId=%s".formatted(lockId));
							return new Kleisli<A, Boolean>(__ -> CompletableFuture.completedFuture(false));
						}
					})
					.orElse(new Kleisli<>(__ -> CompletableFuture.completedFuture(true)))
			)
			.flatMap(isLockAvailable -> {
				if (isLockAvailable) {
					return lockDao.insert(databaseLock).flatMap(__ -> lockDao.getById(lockId));
				} else {
					return new Kleisli<>(__ -> CompletableFuture.completedFuture(Optional.empty()));
				}
			})
			.map(maybeDatabaseLock -> maybeDatabaseLock.map(DatabaseLockService::toLock));

		return transactor.transaction(acquireLock)
			.exceptionally(throwable -> {
				logger.warn("Unable to acquire lock for lockId=%s".formatted(lockId), throwable);
				return Optional.empty();
			});
	}

	@Override
	public CompletableFuture<Boolean> release(String lockId) {
		return transactor.transaction(lockDao.deleteById(lockId));
	}

	private static Lock toLock(DatabaseLock databaseLock) {
		return new Lock(databaseLock.getId(), databaseLock.getAcquiredAt(), databaseLock.getExpiresAt());
	}
}
