package com.ruchij.development.providers;

import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;

public interface ConfigurationProvider {
	RedisConfiguration redisConfiguration();

	ElasticsearchConfiguration elasticsearchConfiguration();

	DatabaseConfiguration databaseConfiguration();
}