package com.ruchij.migration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.ruchij.migration.config.MigrationConfiguration;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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

        run(migrationConfiguration);
    }

    public static void run(MigrationConfiguration migrationConfiguration) throws Exception {
        try (ElasticsearchClientBuilder elasticsearchClientBuilder = new ElasticsearchClientBuilder(migrationConfiguration.elasticsearchConfiguration())) {
            ElasticsearchClient elasticsearchClient = elasticsearchClientBuilder.buildClient();
            ElasticsearchIndicesClient elasticsearchIndicesClient = elasticsearchClient.indices();

            for (String index : INDICES) {
                BooleanResponse existsResponse = elasticsearchIndicesClient.exists(ExistsRequest.of(builder -> builder.index(index)));

                if (!existsResponse.value()) {
                    CreateIndexRequest createIndexRequest = CreateIndexRequest.of(builder -> builder.index(index));
                    elasticsearchIndicesClient.create(createIndexRequest);

                    logger.info("Created index=%s for Elasticsearch".formatted(index));
                }
            }
        }

        logger.info("Migration Completed");
    }
}
