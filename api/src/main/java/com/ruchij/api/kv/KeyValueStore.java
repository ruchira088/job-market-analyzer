package com.ruchij.api.kv;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface KeyValueStore extends AutoCloseable {
    CompletableFuture<Boolean> put(String key, String value);

    CompletableFuture<Optional<String>> get(String key);

    CompletableFuture<Boolean> delete(String key);
}
