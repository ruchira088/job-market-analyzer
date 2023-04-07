package com.ruchij.crawler.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record Kleisli<A, B>(Function<A, CompletableFuture<B>> value) {
	public CompletableFuture<B> run(A input) {
		return value.apply(input);
	}

	public <C> Kleisli<A, C> flatMap(Function<B, Kleisli<A, C>> f) {
		return new Kleisli<>(input -> run(input).thenCompose(b -> f.apply(b).run(input)));
	}

	public <C> Kleisli<A, C> map(Function<B, C> f) {
		return new Kleisli<>(input -> run(input).thenApply(f));
	}

	public <C> Kleisli<A, C> semiFlatMap(Function<B, CompletableFuture<C>> f) {
		return flatMap(b -> new Kleisli<>(__ -> f.apply(b)));
	}
}
