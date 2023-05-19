package com.ruchij.migration.config;

import com.typesafe.config.Config;

public record MigrationConfiguration(ElasticsearchConfiguration elasticsearchConfiguration,
                                     DatabaseConfiguration databaseConfiguration) {
	public static MigrationConfiguration parse(Config config) {
		return new MigrationConfiguration(
			ElasticsearchConfiguration.parse(config.getConfig("elasticsearch")),
			DatabaseConfiguration.parse(config.getConfig("database"))
		);
	}
}
