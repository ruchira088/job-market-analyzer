package com.ruchij.migration;

import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.ruchij.migration.containers.ElasticsearchContainer;
import com.ruchij.migration.containers.PostgresContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MigrationAppTest {

	@Test
	void runDatabaseMigration() {
		try (PostgresContainer postgresContainer = new PostgresContainer()) {
			DatabaseConfiguration databaseConfiguration = postgresContainer.databaseConfiguration();

			Assertions.assertDoesNotThrow(() -> MigrationApp.runDatabaseMigration(databaseConfiguration));
		}
	}

	@Test
	void runElasticsearchMigration() {
		try (ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer()) {
			ElasticsearchConfiguration elasticsearchConfiguration = elasticsearchContainer.elasticsearchConfiguration();

			Assertions.assertDoesNotThrow(() -> MigrationApp.runElasticsearchMigration(elasticsearchConfiguration));
		}
	}

}