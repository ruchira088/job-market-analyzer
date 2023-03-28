package com.ruchij.migration.containers;

import com.ruchij.migration.config.DatabaseConfiguration;

public class PostgresContainer extends org.testcontainers.containers.PostgreSQLContainer<PostgresContainer> {
	private static final String USER = "my-user";
	private static final String PASSWORD = "my-password";
	private static final String DB_NAME = "job-market-analyzer";

	public PostgresContainer() {
		super("postgres:15.2");
		addEnv("POSTGRES_DB", DB_NAME);
		addEnv("POSTGRES_USER", USER);
		addEnv("POSTGRES_PASSWORD", PASSWORD);
	}

	public DatabaseConfiguration databaseConfiguration() {
		start();

		return new DatabaseConfiguration(
			"jdbc:postgresql://%s:%s/%s".formatted(getHost(), getMappedPort(5432), DB_NAME),
			USER,
			PASSWORD
		);
	}

}
