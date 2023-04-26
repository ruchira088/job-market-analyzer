package com.ruchij.migration.containers;

import com.ruchij.migration.config.ElasticsearchConfiguration;

import java.util.Optional;

public class ElasticsearchContainer extends org.testcontainers.elasticsearch.ElasticsearchContainer {
	private static final String PASSWORD = "my-password";

	public ElasticsearchContainer() {
		super("docker.elastic.co/elasticsearch/elasticsearch:8.7.0");
		addEnv("xpack.security.http.ssl.enabled", "false");
		addEnv("ELASTIC_PASSWORD", PASSWORD);
	}

	public ElasticsearchConfiguration elasticsearchConfiguration() {
		start();

		return new ElasticsearchConfiguration(
			getHost(),
			getMappedPort(9200),
			"docker",
			Optional.of(new ElasticsearchConfiguration.Credentials("elastic", PASSWORD))
		);
	}
}
