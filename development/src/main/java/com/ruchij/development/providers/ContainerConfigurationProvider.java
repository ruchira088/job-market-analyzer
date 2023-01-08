package com.ruchij.development.providers;

import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.api.containers.RedisContainer;
import com.ruchij.migration.MigrationApp;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.ruchij.migration.config.MigrationConfiguration;
import com.ruchij.migration.containers.ElasticsearchContainer;
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

        MigrationConfiguration migrationConfiguration = new MigrationConfiguration(elasticsearchConfiguration);

        try {
            MigrationApp.run(migrationConfiguration);
        } catch (Exception exception) {
            throw new RuntimeException("Error occurred during migration", exception);
        }

        logger.info("Migration completed");

        return elasticsearchConfiguration;
    }
}
