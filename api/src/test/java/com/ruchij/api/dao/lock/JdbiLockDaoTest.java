package com.ruchij.api.dao.lock;

import com.ruchij.api.dao.lock.models.DatabaseLock;
import com.ruchij.crawler.containers.JdbiContainer;
import com.ruchij.crawler.dao.transaction.JdbiTransactor;
import com.ruchij.crawler.utils.Kleisli;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Testcontainers
class JdbiLockDaoTest {

	@Container
	private final JdbiContainer jdbiContainer = new JdbiContainer();

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void shouldInsertDatabaseLock() throws Exception {
		JdbiTransactor jdbiTransactor = jdbiContainer.jdbiTransactor();

		JdbiLockDao jdbiLockDao = new JdbiLockDao();
		Instant instant = Instant.parse("2023-05-29T09:26:54.499Z");

		DatabaseLock databaseLock =
			new DatabaseLock("my-test-lock", instant, instant.plusSeconds(10));

		Kleisli<Handle, Optional<DatabaseLock>> dbOperation =
			jdbiLockDao.insert(databaseLock)
				.flatMap(__ -> jdbiLockDao.getById(databaseLock.getId()));

		Optional<DatabaseLock> result = jdbiTransactor.transaction(dbOperation).get();
		Assertions.assertEquals(databaseLock, result.get());
	}
}