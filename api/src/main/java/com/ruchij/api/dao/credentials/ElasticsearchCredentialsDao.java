package com.ruchij.api.dao.credentials;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.ruchij.api.dao.credentials.models.Credentials;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ElasticsearchCredentialsDao implements CredentialsDao {
    private static final String INDEX = "credentials";
    private final ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchCredentialsDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
    }

    @Override
    public CompletableFuture<String> insert(Credentials credentials) {
        IndexRequest<Credentials> indexRequest =
            IndexRequest.of(builder -> builder.index(INDEX).id(credentials.userId()));

        return elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
    }

    @Override
    public CompletableFuture<Optional<Credentials>> findByUserId(String userId) {
        GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(userId));

        return elasticsearchAsyncClient.get(getRequest, Credentials.class)
            .thenApply(getResponse -> Optional.ofNullable(getResponse.source()));
    }
}
