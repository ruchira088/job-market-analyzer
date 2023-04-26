package com.ruchij.development.providers;

import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;

import java.util.Optional;

public class DockerComposeConfigurationProvider implements ConfigurationProvider {
	@Override
	public RedisConfiguration redisConfiguration() {
		return new RedisConfiguration("localhost", 6379, Optional.of("my-redis-password"));
	}

	@Override
	public ElasticsearchConfiguration elasticsearchConfiguration() {
		return new ElasticsearchConfiguration(
			"localhost",
			9200,
			"docker-compose",
			Optional.of(
				new ElasticsearchConfiguration.Credentials("elastic", "my-password")
			)
		);
	}

	@Override
	public DatabaseConfiguration databaseConfiguration() {
		return new DatabaseConfiguration("jdbc:postgresql://localhost:5432/job-market-analyzer", "admin", "password");
	}
}
