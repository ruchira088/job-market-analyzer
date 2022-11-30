package com.ruchij.test;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.ruchij.crawler.utils.JsonUtils;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public interface ElasticsearchTest {
    void run(ElasticsearchAsyncClient elasticsearchAsyncClient) throws Exception;

    static void run(ElasticsearchTest elasticsearchTest) throws Exception {
        try (ElasticsearchContainer elasticsearchContainer =
                 new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.5.2")
                     .withEnv("xpack.security.enabled", "false")
        ) {
            elasticsearchContainer.start();

            String host = elasticsearchContainer.getHost();
            Integer port = elasticsearchContainer.getMappedPort(9200);

            ElasticsearchConfiguration elasticsearchConfiguration = new ElasticsearchConfiguration(host, port);

            try (ElasticsearchClientBuilder elasticsearchClientBuilder =
                     new ElasticsearchClientBuilder(elasticsearchConfiguration, new JacksonJsonpMapper(JsonUtils.objectMapper))) {
                ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();

                elasticsearchTest.run(elasticsearchAsyncClient);
            }
        }
    }
}
