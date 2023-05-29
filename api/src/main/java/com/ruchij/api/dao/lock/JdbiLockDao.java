package com.ruchij.api.dao.lock;

import com.ruchij.api.dao.lock.models.DatabaseLock;
import com.ruchij.crawler.utils.Kleisli;
import org.jdbi.v3.core.Handle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbiLockDao implements LockDao<Handle> {
	private static String SQL_INSERT =
		"INSERT INTO database_lock(id, acquired_at, expires_at) VALUES (:id, :acquiredAt, :expiresAt)";

	private static String SQL_SELECT = "SELECT id, acquired_at, expires_at FROM database_lock";

	@Override
	public Kleisli<Handle, Integer> insert(DatabaseLock databaseLock) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(handle.createUpdate(SQL_INSERT).bindBean(databaseLock).execute())
		);
	}

	@Override
	public Kleisli<Handle, Boolean> deleteById(String lockId) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
					handle.createUpdate("DELETE FROM database_lock WHERE id = :id").bind("id", lockId).execute()
				)
				.thenApply(result -> result == 1)
		);
	}

	@Override
	public Kleisli<Handle, Optional<DatabaseLock>> getById(String lockId) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE id = :id".formatted(SQL_SELECT))
					.bind("id", lockId)
					.mapToBean(DatabaseLock.class)
					.findOne()
			)
		);
	}

	@Override
	public Kleisli<Handle, List<DatabaseLock>> expiredBefore(Instant instant) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE expires_at < :instant".formatted(SQL_SELECT))
					.bind("instant", instant)
					.mapToBean(DatabaseLock.class)
					.list()
			)
		);
	}
}
