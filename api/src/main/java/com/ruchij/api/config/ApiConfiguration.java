package com.ruchij.api.config;

import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.typesafe.config.Config;

public record ApiConfiguration(
    ElasticsearchConfiguration elasticsearchConfiguration,
    RedisConfiguration redisConfiguration,
    ApiSecurityConfiguration apiSecurityConfiguration,
    HttpConfiguration httpConfiguration
) {
    public static ApiConfiguration parse(Config config) {
        return new ApiConfiguration(
            ElasticsearchConfiguration.parse(config.getConfig("elasticsearch")),
            RedisConfiguration.parse(config.getConfig("redis")),
            ApiSecurityConfiguration.parse(config.getConfig("security")),
            HttpConfiguration.parse(config.getConfig("http"))
        );
    }
}
