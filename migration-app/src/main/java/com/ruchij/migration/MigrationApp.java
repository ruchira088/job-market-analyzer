package com.ruchij.migration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.ruchij.migration.config.MigrationConfiguration;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationApp {
	private static final String[] INDICES = {
		"users",
		"credentials",
		"linkedin_credentials",
		"jobs",
		"crawler_tasks"
	};

	private static final Logger logger = LoggerFactory.getLogger(MigrationApp.class);

	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load();
		MigrationConfiguration migrationConfiguration = MigrationConfiguration.parse(config);

		runMigration(migrationConfiguration);
	}

	public static void runMigration(MigrationConfiguration migrationConfiguration) throws Exception {
		runDatabaseMigration(migrationConfiguration.databaseConfiguration());
		runElasticsearchMigration(migrationConfiguration.elasticsearchConfiguration());
	}

	public static void runDatabaseMigration(DatabaseConfiguration databaseConfiguration) {
		logger.info("Database migration started");

		Flyway flyway =
			Flyway.configure()
				.dataSource(databaseConfiguration.url(), databaseConfiguration.user(), databaseConfiguration.password())
				.load();

		MigrateResult migrateResult = flyway.migrate();

		logger.info("Database migration completed (targetSchemaVersion=%s, migrationsExecuted=%s, success=%s)"
			.formatted(migrateResult.targetSchemaVersion, migrateResult.migrationsExecuted, migrateResult.success)
		);
	}

	public static void runElasticsearchMigration(ElasticsearchConfiguration elasticsearchConfiguration) throws Exception {
		logger.info("Elasticsearch migration started");

		try (ElasticsearchClientBuilder elasticsearchClientBuilder = new ElasticsearchClientBuilder(elasticsearchConfiguration)) {
			ElasticsearchClient elasticsearchClient = elasticsearchClientBuilder.buildClient();
			ElasticsearchIndicesClient elasticsearchIndicesClient = elasticsearchClient.indices();

			for (String index : INDICES) {
				String indexName = "%s-%s".formatted(elasticsearchConfiguration.indexPrefix(), index);
				BooleanResponse existsResponse = elasticsearchIndicesClient.exists(ExistsRequest.of(builder -> builder.index(indexName)));

				if (!existsResponse.value()) {
					CreateIndexRequest createIndexRequest = CreateIndexRequest.of(builder -> builder.index(indexName));
					elasticsearchIndicesClient.create(createIndexRequest);

					logger.info("Created index=%s for Elasticsearch".formatted(indexName));
				}
			}
		}

		logger.info("Elasticsearch migration completed");
	}
}
