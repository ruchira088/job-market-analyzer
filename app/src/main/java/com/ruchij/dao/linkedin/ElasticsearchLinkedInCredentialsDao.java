package com.ruchij.dao.linkedin;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ruchij.dao.linkedin.models.LinkedInCredentials;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ElasticsearchLinkedInCredentialsDao implements LinkedInCredentialsDao {
    private static final String INDEX = "linkedIn-credentials";

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchLinkedInCredentialsDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
    }

    @Override
    public CompletableFuture<String> insert(LinkedInCredentials linkedInCredentials) {
        IndexRequest<LinkedInCredentials> indexRequest =
            IndexRequest.of(builder -> builder.index(INDEX).id(linkedInCredentials.getUserId()).document(linkedInCredentials));

        return elasticsearchAsyncClient.index(indexRequest).thenApplyAsync(WriteResponseBase::id);
    }

    @Override
    public Flowable<LinkedInCredentials> getAll() {
        return Flowable.create(emitter -> {
            int page = 0;
            int size = 100;
            boolean completed = false;

            while (!completed) {
                List<LinkedInCredentials> linkedInCredentialsList =
                    getAll(page, size).get(20, TimeUnit.SECONDS);

                for (LinkedInCredentials linkedInCredentials : linkedInCredentialsList) {
                    emitter.onNext(linkedInCredentials);
                }

                if (linkedInCredentialsList.size() < size) {
                    completed = true;
                    emitter.onComplete();
                } else {
                    page++;
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    private CompletableFuture<List<LinkedInCredentials>> getAll(int page, int size) {
        SearchRequest searchRequest = SearchRequest.of(builder -> builder.index(INDEX).size(size).from(page * size));

        return elasticsearchAsyncClient.search(searchRequest, LinkedInCredentials.class)
            .thenApplyAsync(linkedInCredentialsSearchResponse ->
                linkedInCredentialsSearchResponse.hits().hits().stream().map(Hit::source).toList()
            );
    }

    @Override
    public CompletableFuture<Optional<LinkedInCredentials>> findByUserId(String userId) {
        GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(userId));

        return elasticsearchAsyncClient.get(getRequest, LinkedInCredentials.class)
            .thenApplyAsync(linkedInCredentialsGetResponse -> Optional.ofNullable(linkedInCredentialsGetResponse.source()));
    }
}
