package com.ruchij.migration.containers;

import com.ruchij.migration.config.ElasticsearchConfiguration;

public class ElasticsearchContainer extends org.testcontainers.elasticsearch.ElasticsearchContainer {
    public ElasticsearchContainer() {
        super("docker.elastic.co/elasticsearch/elasticsearch:8.5.2");
        addEnv("xpack.security.enabled", "false");
    }

    public ElasticsearchConfiguration elasticsearchConfiguration() {
        start();

        return new ElasticsearchConfiguration(getHost(), getMappedPort(9200));
    }
}
