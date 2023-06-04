package com.ruchij.crawler.containers;

import com.ruchij.crawler.dao.jdbi.JdbiInitializer;
import com.ruchij.crawler.dao.transaction.JdbiTransactor;
import com.ruchij.migration.MigrationApp;
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.containers.PostgresContainer;
import org.jdbi.v3.core.Jdbi;

public class JdbiContainer extends PostgresContainer {
	public JdbiTransactor jdbiTransactor() {
		DatabaseConfiguration databaseConfiguration = databaseConfiguration();
		MigrationApp.runDatabaseMigration(databaseConfiguration);

		Jdbi jdbi = Jdbi.create(databaseConfiguration.url(), databaseConfiguration.user(), databaseConfiguration.password());
		JdbiInitializer.initialize(jdbi);

		return new JdbiTransactor(jdbi);
	}
}
