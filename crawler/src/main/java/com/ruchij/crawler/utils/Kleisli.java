package com.ruchij.crawler.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public record Kleisli<A, B>(Function<A, CompletableFuture<B>> value) {
	public CompletableFuture<B> run(A input) {
		return value.apply(input);
	}

	public <C> Kleisli<A, C> flatMap(Function<B, Kleisli<A, C>> next) {
		return new Kleisli<>(input -> value.apply(input).thenCompose(b -> next.apply(b).run(input)));
	}

	public <C> Kleisli<A, C> map(Function<B, C> next) {
		return new Kleisli<>(input -> value.apply(input).thenApply(next));
	}

	public <C> Kleisli<A, C> semiFlatMap(Function<B, CompletableFuture<C>> next) {
		return flatMap(b -> lift(() -> next.apply(b)));
	}

	public static <A, B> Kleisli<A, B> lift(Supplier<CompletableFuture<B>> supplier) {
		return new Kleisli<>(__ -> supplier.get());
	}
}
