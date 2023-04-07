package com.ruchij.crawler.utils;

import java.util.function.Function;

public record ReaderMonad<A, B>(Function<A, B> value) {
	public B run(A input) {
		return value.apply(input);
	}

	public <C> ReaderMonad<A, C> flatMap(Function<B, ReaderMonad<A, C>> f) {
		return new ReaderMonad<>(input -> f.apply(run(input)).run(input));
	}

	public <C> ReaderMonad<A, C> map(Function<B, C> f) {
		return new ReaderMonad<>(input -> f.apply(run(input)));
	}
}
