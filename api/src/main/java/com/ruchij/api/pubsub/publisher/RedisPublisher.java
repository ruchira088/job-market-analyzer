package com.ruchij.api.pubsub.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.concurrent.CompletableFuture;

public class RedisPublisher implements Publisher, AutoCloseable {
	private final RedisClient redisClient;
	private final RedisAsyncCommands<String, String> redisAsyncCommands;

	private final ObjectMapper objectMapper;

	public RedisPublisher(String redisConnectionString, ObjectMapper objectMapper) {
		this.redisClient = RedisClient.create(redisConnectionString);
		this.redisAsyncCommands = redisClient.connect().async();
		this.objectMapper = objectMapper;
	}

	@Override
	public <T> CompletableFuture<String> publish(String topicName, T data) {
		try {
			String serializedValue = this.objectMapper.writeValueAsString(data);
			return this.redisAsyncCommands.publish(topicName, serializedValue)
				.thenApply(Object::toString)
				.toCompletableFuture();
		} catch (JsonProcessingException jsonProcessingException) {
			return CompletableFuture.failedFuture(jsonProcessingException);
		}
	}

	@Override
	public void close() {
		this.redisClient.close();
	}
}
