package com.ruchij.crawler.dao.transaction;

import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.ReaderMonad;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.concurrent.CompletableFuture;

public class JdbiTransactor implements Transactor<Handle> {
	private final Jdbi jdbi;

	public JdbiTransactor(Jdbi jdbi) {
		this.jdbi = jdbi;
	}

	@Override
	public <B> CompletableFuture<B> transaction(Kleisli<Handle, B> operations) {
		Handle handle = jdbi.open();

		try {
			handle.begin();

			CompletableFuture<B> completableFuture = operations.run(handle);

			completableFuture.whenComplete((result, throwable) -> {
				if (throwable == null) {
					handle.commit();
				} else {
					handle.rollback();
				}
				handle.close();
			});

			return completableFuture;
		} catch (Exception exception) {
			if (handle.isInTransaction()) {
				handle.rollback();
			}

			handle.close();

			return CompletableFuture.failedFuture(exception);
		}
	}

	@Override
	public <B> B transaction(ReaderMonad<Handle, B> operations) {
		return jdbi.inTransaction(operations::run);
	}
}
