package com.ruchij.service.random;

import java.util.UUID;
import java.util.function.Function;

public interface RandomGenerator<T> {
    T generate();

    default <R> RandomGenerator<R> map(Function<T, R> mapper) {
        return () -> mapper.apply(this.generate());
    }

    static RandomGenerator<UUID> uuidGenerator() {
        return UUID::randomUUID;
    }
}
