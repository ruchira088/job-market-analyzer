package com.ruchij.api.dao.lock;

import com.ruchij.api.dao.lock.models.DatabaseLock;
import com.ruchij.crawler.utils.Kleisli;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LockDao<A> {
	Kleisli<A, Integer> insert(DatabaseLock databaseLock);

	Kleisli<A, Boolean> deleteById(String lockId);

	Kleisli<A, Optional<DatabaseLock>> getById(String lockId);

	Kleisli<A, List<DatabaseLock>> expiredBefore(Instant instant);
}
