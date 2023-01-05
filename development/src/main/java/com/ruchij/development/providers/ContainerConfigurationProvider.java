package com.ruchij.development.providers;

import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.api.containers.RedisContainer;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.ruchij.migration.containers.ElasticsearchContainer;

public class ContainerConfigurationProvider implements ConfigurationProvider {
    @Override
    public RedisConfiguration redisConfiguration() {
        return new RedisContainer().redisConfiguration();
    }

    @Override
    public ElasticsearchConfiguration elasticsearchConfiguration() {
        return new ElasticsearchContainer().elasticsearchConfiguration();
    }
}
