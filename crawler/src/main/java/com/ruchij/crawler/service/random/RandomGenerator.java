package com.ruchij.crawler.service.random;

import java.util.UUID;
import java.util.function.Function;

public interface RandomGenerator<T> {
	static RandomGenerator<UUID> uuidGenerator() {
		return UUID::randomUUID;
	}

	T generate();

	default <R> RandomGenerator<R> map(Function<T, R> mapper) {
		return () -> mapper.apply(this.generate());
	}
}
