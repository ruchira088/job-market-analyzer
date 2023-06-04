package com.ruchij.api.kv;

import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.api.containers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisKeyValueStoreTest {
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	void shouldInsertAndRetrieveKeyValuePair() throws Exception {
		run(redisKeyValueStore ->
			redisKeyValueStore.put("A", "APPLE", Optional.empty())
				.thenCompose(__ -> redisKeyValueStore.get("A"))
				.thenAccept(maybeValue -> assertEquals(Optional.of("APPLE"), maybeValue))
		);
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	void shouldExpireKeyAfterTtl() throws Exception {
		run(redisKeyValueStore ->
			redisKeyValueStore.put("B", "BAT", Optional.of(Duration.ofSeconds(1)))
				.thenCompose(__ -> redisKeyValueStore.get("B"))
				.thenAccept(maybeValue -> assertEquals(Optional.of("BAT"), maybeValue))
				.thenCompose(__ -> sleep(Duration.ofSeconds(2)))
				.thenCompose(__ -> redisKeyValueStore.get("B"))
				.thenAccept(maybeValue -> assertEquals(Optional.empty(), maybeValue))
		);
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	void shouldDeleteKey() throws Exception {
		run(redisKeyValueStore ->
			redisKeyValueStore.put("C", "CAT", Optional.empty())
				.thenCompose(__ -> redisKeyValueStore.get("C"))
				.thenAccept(maybeValue -> assertEquals(Optional.of("CAT"), maybeValue))
				.thenCompose(__ -> redisKeyValueStore.delete("C"))
				.thenCompose(__ -> redisKeyValueStore.get("C"))
				.thenAccept(maybeValue -> assertEquals(Optional.empty(), maybeValue))
		);
	}

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS)
	void shouldUpdateExistingValue() throws Exception {
		run(redisKeyValueStore ->
			redisKeyValueStore.put("D", "DOG", Optional.empty())
				.thenCompose(__ -> redisKeyValueStore.get("D"))
				.thenAccept(maybeValue -> assertEquals(Optional.of("DOG"), maybeValue))
				.thenCompose(__ -> redisKeyValueStore.put("D", "DEAR", Optional.empty()))
				.thenCompose(__ -> redisKeyValueStore.get("D"))
				.thenAccept(maybeValue -> assertEquals(Optional.of("DEAR"), maybeValue))
		);
	}

	private CompletableFuture<Void> sleep(Duration duration) {
		CompletableFuture<Void> completableFuture = new CompletableFuture<>();
		scheduledExecutorService.schedule(
			() -> completableFuture.complete(null),
			duration.toMillis(),
			TimeUnit.MILLISECONDS
		);

		return completableFuture;
	}

	private void run(Function<RedisKeyValueStore, CompletableFuture<?>> action) throws Exception {
		try (RedisContainer redisContainer = new RedisContainer()) {
			RedisConfiguration redisConfiguration = redisContainer.redisConfiguration();
			RedisKeyValueStore redisKeyValueStore = new RedisKeyValueStore(redisConfiguration.uri());

			action.apply(redisKeyValueStore).get();
		}
	}

}