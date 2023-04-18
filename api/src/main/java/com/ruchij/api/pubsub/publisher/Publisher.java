package com.ruchij.api.pubsub.publisher;

import java.util.concurrent.CompletableFuture;

public interface Publisher {
	<T> CompletableFuture<String> publish(String topicName, T data);
}
