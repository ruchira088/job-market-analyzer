package com.ruchij.crawler.dao.transaction;

import com.ruchij.crawler.utils.Kleisli;

import java.util.concurrent.CompletableFuture;

public interface Transactor<A> {
	<B> CompletableFuture<B> transaction(Kleisli<A, B> operations);
}
