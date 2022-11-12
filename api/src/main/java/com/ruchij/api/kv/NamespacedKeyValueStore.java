package com.ruchij.api.kv;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NamespacedKeyValueStore implements KeyValueStore {
    private final KeyValueStore keyValueStore;
    private final String namespace;

    public NamespacedKeyValueStore(KeyValueStore keyValueStore, String namespace) {
        this.keyValueStore = keyValueStore;
        this.namespace = namespace;
    }

    @Override
    public CompletableFuture<Boolean> put(String key, String value) {
        return keyValueStore.put(namespacedKey(key), value);
    }

    @Override
    public CompletableFuture<Optional<String>> get(String key) {
        return keyValueStore.get(namespacedKey(key));
    }

    @Override
    public CompletableFuture<Boolean> delete(String key) {
        return keyValueStore.delete(namespacedKey(key));
    }

    private String namespacedKey(String key) {
        return namespace + "_" + key;
    }

    @Override
    public void close() throws Exception {
        keyValueStore.close();
    }
}
