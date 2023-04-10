package com.ruchij.migration;

import com.ruchij.migration.config.DatabaseConfiguration;
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

}