package com.ruchij.api.config;

import com.ruchij.config.ElasticsearchConfiguration;

public record ApiConfiguration(
    ElasticsearchConfiguration elasticsearchConfiguration,
    RedisConfiguration redisConfiguration,
    ApiSecurityConfiguration apiSecurityConfiguration
) {
}
