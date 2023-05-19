package com.ruchij.crawler.dao.transaction;

import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.ReaderMonad;

import java.util.concurrent.CompletableFuture;

public interface Transactor<A> {
	<B> CompletableFuture<B> transaction(Kleisli<A, B> operations);

	<B> B transaction(ReaderMonad<A, B> operations);
}
