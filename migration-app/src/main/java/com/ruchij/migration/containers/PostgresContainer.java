package com.ruchij.migration.containers;

import com.ruchij.migration.config.DatabaseConfiguration;

public class PostgresContainer extends org.testcontainers.containers.PostgreSQLContainer<PostgresContainer> {
	private static final String USER = "my-user";
	private static final String PASSWORD = "my-password";
	private static final String DB_NAME = "job-market-analyzer";

	public PostgresContainer() {
		super("postgres:15.2");
		withDatabaseName(DB_NAME);
		withUsername(USER);
		withPassword(PASSWORD);
	}

	public DatabaseConfiguration databaseConfiguration() {
		start();

		return new DatabaseConfiguration(
			getJdbcUrl(),
			USER,
			PASSWORD
		);
	}

}
