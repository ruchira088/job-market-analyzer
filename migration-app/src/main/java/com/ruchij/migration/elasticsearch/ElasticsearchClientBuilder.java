package com.ruchij.migration.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class ElasticsearchClientBuilder implements AutoCloseable {
    private final RestClient restClient;
    private final ElasticsearchTransport elasticsearchTransport;

    public ElasticsearchClientBuilder(ElasticsearchConfiguration elasticsearchConfiguration) {
        this(elasticsearchConfiguration, new JacksonJsonpMapper());
    }

    public ElasticsearchClientBuilder(ElasticsearchConfiguration elasticsearchConfiguration, JsonpMapper jsonpMapper) {
        HttpHost elasticsearchHost =
            new HttpHost(elasticsearchConfiguration.host(), elasticsearchConfiguration.port());

        restClient = RestClient.builder(elasticsearchHost).build();
        elasticsearchTransport = new RestClientTransport(restClient, jsonpMapper);
    }

    public ElasticsearchAsyncClient buildAsyncClient() {
        return new ElasticsearchAsyncClient(elasticsearchTransport);
    }

    public ElasticsearchClient buildClient() {
        return new ElasticsearchClient(elasticsearchTransport);
    }

    @Override
    public void close() throws Exception {
        restClient.close();
        elasticsearchTransport.close();
    }
}
