package com.ruchij.api.pubsub;

import com.ruchij.api.containers.RedisContainer;
import com.ruchij.api.pubsub.publisher.RedisPublisher;
import com.ruchij.api.pubsub.subscriber.RedisSubscriber;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ruchij.crawler.utils.JsonUtils.objectMapper;

public class RedisPubSubTest {
	public record SampleRedisMessage(int id) {
	}

	@Test
	@Timeout(value = 5, unit = TimeUnit.SECONDS)
	void publishAndSubscribe() throws InterruptedException {
		try (RedisContainer redisContainer = new RedisContainer()) {
			String redisConnectionString = redisContainer.redisConfiguration().uri();
			String topicName = "my-redis-topic";
			int messageCount = 10;

			CountDownLatch countDownLatch = new CountDownLatch(messageCount);
			Queue<SampleRedisMessage> subscribedMessages = new ArrayBlockingQueue<>(messageCount);

			try(RedisSubscriber redisSubscriber = new RedisSubscriber(redisConnectionString, objectMapper);
			    RedisPublisher redisPublisher = new RedisPublisher(redisConnectionString, objectMapper)) {
				redisSubscriber
					.subscribe(topicName, SampleRedisMessage.class, "my-subscriber")
					.subscribe(value -> {
						subscribedMessages.add(value);
						countDownLatch.countDown();
					});

				Flowable<SampleRedisMessage> publishedMessages =
					Flowable.range(0, messageCount).map(SampleRedisMessage::new);

				publishedMessages
					.concatMap(message ->
						Flowable.fromCompletionStage(redisPublisher.publish(topicName, message))
					)
					.blockingSubscribe();

				countDownLatch.await();

				Assertions.assertEquals(
					publishedMessages.toList().blockingGet(),
					subscribedMessages.stream().toList()
				);
			}
		}
	}
}
