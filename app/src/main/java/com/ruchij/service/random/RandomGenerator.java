package com.ruchij.service.random;

import java.util.UUID;

public interface RandomGenerator<T> {
    T generate();

    static RandomGenerator<String> idGenerator() {
        return () -> UUID.randomUUID().toString();
    }
}
