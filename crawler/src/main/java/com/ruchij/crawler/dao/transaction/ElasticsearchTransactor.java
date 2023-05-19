package com.ruchij.crawler.dao.transaction;

import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.ReaderMonad;

import java.util.concurrent.CompletableFuture;

public class ElasticsearchTransactor implements Transactor<Void> {
	@Override
	public <B> CompletableFuture<B> transaction(Kleisli<Void, B> operations) {
		return operations.run(null);
	}

	@Override
	public <B> B transaction(ReaderMonad<Void, B> operations) {
		return operations.run(null);
	}
}
