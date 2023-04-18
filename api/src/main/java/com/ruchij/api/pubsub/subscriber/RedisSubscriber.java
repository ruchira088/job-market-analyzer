package com.ruchij.api.pubsub.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import reactor.core.publisher.Flux;

public class RedisSubscriber implements Subscriber, AutoCloseable {
	private final RedisClient redisClient;

	private final ObjectMapper objectMapper;

	public RedisSubscriber(String redisConnectionString, ObjectMapper objectMapper) {
		this.redisClient = RedisClient.create(redisConnectionString);
		this.objectMapper = objectMapper;
	}

	@Override
	public <T> Flowable<T> subscribe(String topicName, Class<T> clazz, String subscriberName) {
		return new Flowable<>() {
			@Override
			protected void subscribeActual(org.reactivestreams.@NonNull Subscriber<? super T> subscriber) {
				StatefulRedisPubSubConnection<String, String> pubSubConnection = redisClient.connectPubSub();
				RedisPubSubReactiveCommands<String, String> pubSubReactiveCommands = pubSubConnection.reactive();

				pubSubReactiveCommands.subscribe(topicName).flux()
					.thenMany(pubSubReactiveCommands.observeChannels())
					.concatMap(channelMessage -> {
						String serializedMessage = channelMessage.getMessage();
						try {
							T value = objectMapper.readValue(serializedMessage, clazz);
							return Flux.just(value);
						} catch (JsonProcessingException jsonProcessingException) {
							return Flux.error(jsonProcessingException);
						}
					})
					.doFinally(__ -> pubSubConnection.close())
					.subscribe(subscriber);
			}
		};
	}

	@Override
	public void close() {
		this.redisClient.close();
	}
}
