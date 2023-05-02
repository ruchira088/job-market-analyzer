package com.ruchij.api.config;

import com.ruchij.crawler.config.SeleniumConfiguration;
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.typesafe.config.Config;

public record ApiConfiguration(
	ElasticsearchConfiguration elasticsearchConfiguration,
	DatabaseConfiguration databaseConfiguration,
	RedisConfiguration redisConfiguration,
	ApiSecurityConfiguration apiSecurityConfiguration,
	HttpConfiguration httpConfiguration,
	SeleniumConfiguration seleniumConfiguration
) {
	public static ApiConfiguration parse(Config config) {
		return new ApiConfiguration(
			ElasticsearchConfiguration.parse(config.getConfig("elasticsearch")),
			DatabaseConfiguration.parse(config.getConfig("database")),
			RedisConfiguration.parse(config.getConfig("redis")),
			ApiSecurityConfiguration.parse(config.getConfig("security")),
			HttpConfiguration.parse(config.getConfig("http")),
			SeleniumConfiguration.parse(config.getConfig("selenium"))
		);
	}
}
