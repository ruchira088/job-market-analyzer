package com.ruchij.development.providers;

import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.api.containers.RedisContainer;
import com.ruchij.migration.MigrationApp;
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.ruchij.migration.config.MigrationConfiguration;
import com.ruchij.migration.containers.ElasticsearchContainer;
import com.ruchij.migration.containers.PostgresContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerConfigurationProvider implements ConfigurationProvider {
	private static final Logger logger = LoggerFactory.getLogger(ContainerConfigurationProvider.class);

	@Override
	public RedisConfiguration redisConfiguration() {
		return new RedisContainer().redisConfiguration();
	}

	@Override
	public ElasticsearchConfiguration elasticsearchConfiguration() {
		ElasticsearchConfiguration elasticsearchConfiguration =
			new ElasticsearchContainer().elasticsearchConfiguration();

		try {
			MigrationApp.runElasticsearchMigration(elasticsearchConfiguration);
		} catch (Exception exception) {
			throw new RuntimeException("Error occurred during Elasticsearch migration", exception);
		}

		return elasticsearchConfiguration;
	}

	@Override
	public DatabaseConfiguration databaseConfiguration() {
		DatabaseConfiguration databaseConfiguration = new PostgresContainer().databaseConfiguration();

		try {
			MigrationApp.runDatabaseMigration(databaseConfiguration);
		} catch (Exception exception) {
			throw new RuntimeException("Error occurred during Database migration", exception);
		}

		return databaseConfiguration;
	}
}
