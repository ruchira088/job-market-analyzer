package com.ruchij.api.containers;

import com.ruchij.api.config.RedisConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.util.Optional;

public class RedisContainer extends GenericContainer<RedisContainer> {
    private static final String REDIS_PASSWORD = "my-redis-password";
    private static final int REDIS_PORT = 6379;

    public RedisContainer() {
        super("bitnami/redis:7.0");
        addEnv("REDIS_PASSWORD", REDIS_PASSWORD);
        addExposedPort(REDIS_PORT);
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*Ready to accept connections.*\\n"));
    }

    public RedisConfiguration redisConfiguration() {
        start();

        return new RedisConfiguration(getHost(), getMappedPort(REDIS_PORT), Optional.of(REDIS_PASSWORD));
    }
}
