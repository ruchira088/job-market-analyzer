package com.ruchij.dao.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ruchij.config.ElasticsearchConfiguration;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class ElasticsearchClientBuilder implements AutoCloseable {
    private final RestClient restClient;
    private final ElasticsearchTransport elasticsearchTransport;

    public ElasticsearchClientBuilder(ElasticsearchConfiguration elasticsearchConfiguration) {
        HttpHost elasticsearchHost =
            new HttpHost(elasticsearchConfiguration.host(), elasticsearchConfiguration.port());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        restClient = RestClient.builder(elasticsearchHost).build();
        elasticsearchTransport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
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
