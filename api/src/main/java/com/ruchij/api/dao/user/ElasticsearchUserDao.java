package com.ruchij.api.dao.user;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ruchij.api.dao.user.models.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ElasticsearchUserDao implements UserDao {
    private static final String INDEX = "users";

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchUserDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
    }

    @Override
    public CompletableFuture<String> insert(User user) {
        IndexRequest<User> indexRequest =
            IndexRequest.of(builder -> builder.index(INDEX).id(user.userId()).document(user));

        return elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
    }

    @Override
    public CompletableFuture<Optional<User>> findById(String userId) {
        GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(userId));

        return elasticsearchAsyncClient.get(getRequest, User.class)
            .thenApply(getResponse -> Optional.ofNullable(getResponse.source()));
    }

    @Override
    public CompletableFuture<Optional<User>> findByEmail(String email) {
        SearchRequest searchRequest = SearchRequest.of(builder ->
            builder.index(INDEX)
                .query(queryBuilder ->
                    queryBuilder.term(termQuery ->
                        termQuery
                            .field("email.keyword")
                            .value(email)
                    )
                )
                .size(1)
        );

        return elasticsearchAsyncClient.search(searchRequest, User.class)
            .thenApply(searchResponse -> searchResponse.hits().hits().stream().findFirst().map(Hit::source));
    }
}
