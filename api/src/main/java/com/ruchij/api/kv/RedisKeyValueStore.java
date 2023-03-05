package com.ruchij.api.kv;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RedisKeyValueStore implements KeyValueStore, AutoCloseable {
	private final String SUCCESS_RESPONSE = "OK";

	private final RedisClient redisClient;
	private final StatefulRedisConnection<String, String> redisConnection;
	private final RedisAsyncCommands<String, String> redisAsyncCommands;

	public RedisKeyValueStore(String redisConnectionString) {
		this.redisClient = RedisClient.create(redisConnectionString);
		this.redisConnection = redisClient.connect();
		this.redisAsyncCommands = redisConnection.async();
	}

	@Override
	public CompletableFuture<Boolean> put(String key, String value) {
		return redisAsyncCommands.set(key, value).toCompletableFuture()
			.thenApply(result -> result.equals(SUCCESS_RESPONSE));
	}

	@Override
	public CompletableFuture<Optional<String>> get(String key) {
		return redisAsyncCommands.get(key).toCompletableFuture().thenApply(Optional::ofNullable);
	}

	@Override
	public CompletableFuture<Boolean> delete(String key) {
		return redisAsyncCommands.del(key).toCompletableFuture().thenApply(count -> count == 1);
	}

	public RedisAsyncCommands<String, String> getRedisAsyncCommands() {
		return redisAsyncCommands;
	}

	@Override
	public void close() {
		redisConnection.close();
		redisClient.close();
	}
}
