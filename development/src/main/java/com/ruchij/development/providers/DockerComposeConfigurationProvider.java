package com.ruchij.development.providers;

import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;

import java.util.Optional;

public class DockerComposeConfigurationProvider implements ConfigurationProvider {
    @Override
    public RedisConfiguration redisConfiguration() {
        return new RedisConfiguration("localhost", 6379, Optional.of("my-redis-password"));
    }

    @Override
    public ElasticsearchConfiguration elasticsearchConfiguration() {
        return new ElasticsearchConfiguration("localhost", 9200);
    }
}
