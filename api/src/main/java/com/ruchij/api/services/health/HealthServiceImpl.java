package com.ruchij.api.services.health;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import com.ruchij.api.services.health.models.BuildInformation;
import com.ruchij.api.services.health.models.HealthCheck;
import com.ruchij.api.services.health.models.HealthStatus;
import com.ruchij.api.services.health.models.ServiceInformation;
import com.ruchij.crawler.service.crawler.Crawler;
import com.ruchij.crawler.utils.JsonUtils;
import io.lettuce.core.api.async.RedisAsyncCommands;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HealthServiceImpl implements HealthService {
    private static final Logger logger = LoggerFactory.getLogger(HealthServiceImpl.class);
    private static final Duration TIME_OUT = Duration.ofSeconds(50);

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;
    private final RedisAsyncCommands<String, String> redisAsyncCommands;
    private final OkHttpClient okHttpClient;
    private final Crawler crawler;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Properties properties;
    private final Clock clock;

    public HealthServiceImpl(ElasticsearchAsyncClient elasticsearchAsyncClient,
                             RedisAsyncCommands<String, String> redisAsyncCommands,
                             OkHttpClient okHttpClient,
                             Crawler crawler,
                             ScheduledExecutorService scheduledExecutorService,
                             Properties properties,
                             Clock clock) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
        this.redisAsyncCommands = redisAsyncCommands;
        this.okHttpClient = okHttpClient;
        this.crawler = crawler;
        this.scheduledExecutorService = scheduledExecutorService;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public CompletableFuture<ServiceInformation> serviceInformation() {
        try {
            String javaVersion = properties.getProperty("java.version", "unknown");
            Instant timestamp = clock.instant();

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("build-information.json");
            BuildInformation buildInformation = JsonUtils.objectMapper.readValue(inputStream, BuildInformation.class);

            ServiceInformation serviceInformation =
                new ServiceInformation(
                    buildInformation.name(),
                    buildInformation.version(),
                    javaVersion,
                    buildInformation.gradleVersion(),
                    timestamp,
                    buildInformation.gitBranch(),
                    buildInformation.gitCommit(),
                    buildInformation.buildTimestamp()
                );

            return CompletableFuture.completedFuture(serviceInformation);
        } catch (IOException ioException) {
            return CompletableFuture.failedFuture(ioException);
        }
    }

    @Override
    public CompletableFuture<HealthCheck> healthCheck() {
        CompletableFuture<HealthStatus> elasticsearchHealthStatus =
            race(elasticsearchAsyncClient.ping().thenApply(booleanResponse -> HealthStatus.HEALTHY));

        CompletableFuture<HealthStatus> redisHealthStatus =
            race(redisAsyncCommands.ping().thenApply(response -> HealthStatus.HEALTHY).toCompletableFuture());

        CompletableFuture<HealthStatus> internetConnectivityHealthStatus = race(internetConnectivity());

        CompletableFuture<HealthStatus> linkedInRenderingHealthStatus =
            race(crawler.isHealthy().thenApply(result -> result ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY));


        HealthCheck healthCheck =
            new HealthCheck(
                resolve(elasticsearchHealthStatus),
                resolve(redisHealthStatus),
                resolve(internetConnectivityHealthStatus),
                resolve(linkedInRenderingHealthStatus)
            );

        return CompletableFuture.completedFuture(healthCheck);
    }

    private HealthStatus resolve(CompletableFuture<HealthStatus> completableFuture) {
        try {
            return completableFuture.get();
        } catch (Exception exception) {
            return HealthStatus.UNHEALTHY;
        }
    }

    private CompletableFuture<HealthStatus> race(CompletableFuture<HealthStatus> completableFuture) {
        CompletableFuture<HealthStatus> result = new CompletableFuture<>();

        completableFuture.whenComplete(((healthStatus, throwable) -> {
            if (throwable != null) {
                result.complete(HealthStatus.UNHEALTHY);
            } else {
                result.complete(healthStatus);
            }
        }));

        scheduledExecutorService.schedule(
            () -> {
                if (!completableFuture.isDone()) {
                    result.complete(HealthStatus.UNHEALTHY);
                    completableFuture.cancel(true);
                }
            }, TIME_OUT.toMillis(),
            TimeUnit.MILLISECONDS
        );

        return result;
    }

    private CompletableFuture<HealthStatus> internetConnectivity() {
        CompletableFuture<HealthStatus> completableFuture = new CompletableFuture<>();

        Request httpRequest = new Request.Builder().url("https://ip.ruchij.com").get().build();
        okHttpClient.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException exception) {
                logger.error("Error verifying Internet connectivity", exception);
                completableFuture.complete(HealthStatus.UNHEALTHY);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    completableFuture.complete(HealthStatus.HEALTHY);
                } else {
                    completableFuture.complete(HealthStatus.UNHEALTHY);
                }

                response.close();
            }
        });

        return completableFuture;
    }
}
