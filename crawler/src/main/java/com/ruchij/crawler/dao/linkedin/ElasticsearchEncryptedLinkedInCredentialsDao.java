package com.ruchij.crawler.dao.linkedin;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ruchij.crawler.dao.linkedin.models.EncryptedLinkedInCredentials;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ElasticsearchEncryptedLinkedInCredentialsDao implements EncryptedLinkedInCredentialsDao {
    private static final String INDEX = "linkedin_credentials";

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchEncryptedLinkedInCredentialsDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
    }

    @Override
    public CompletableFuture<String> insert(EncryptedLinkedInCredentials encryptedLinkedInCredentials) {
        IndexRequest<EncryptedLinkedInCredentials> indexRequest =
            IndexRequest.of(builder ->
                builder
                    .index(INDEX)
                    .id(encryptedLinkedInCredentials.userId())
                    .document(encryptedLinkedInCredentials)
            );

        return elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
    }

    @Override
    public Flowable<EncryptedLinkedInCredentials> getAll() {
        return Flowable.create(emitter -> {
            int page = 0;
            int size = 100;
            boolean completed = false;

            while (!completed) {
                List<EncryptedLinkedInCredentials> encryptedLinkedInCredentialsList =
                    getAll(page, size).get(20, TimeUnit.SECONDS);

                for (EncryptedLinkedInCredentials encryptedLinkedInCredentials : encryptedLinkedInCredentialsList) {
                    emitter.onNext(encryptedLinkedInCredentials);
                }

                if (encryptedLinkedInCredentialsList.size() < size) {
                    completed = true;
                    emitter.onComplete();
                } else {
                    page++;
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    private CompletableFuture<List<EncryptedLinkedInCredentials>> getAll(int page, int size) {
        SearchRequest searchRequest = SearchRequest.of(builder -> builder.index(INDEX).size(size).from(page * size));

        return elasticsearchAsyncClient.search(searchRequest, EncryptedLinkedInCredentials.class)
            .thenApply(linkedInCredentialsSearchResponse ->
                linkedInCredentialsSearchResponse.hits().hits().stream().map(Hit::source).toList()
            );
    }

    @Override
    public CompletableFuture<Optional<EncryptedLinkedInCredentials>> findByUserId(String userId) {
        GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(userId));

        return elasticsearchAsyncClient.get(getRequest, EncryptedLinkedInCredentials.class)
            .thenApply(linkedInCredentialsGetResponse -> Optional.ofNullable(linkedInCredentialsGetResponse.source()));
    }

    @Override
    public CompletableFuture<Boolean> deleteByUserId(String userId) {
        DeleteRequest deleteRequest = DeleteRequest.of(builder -> builder.index(INDEX).id(userId));

        return elasticsearchAsyncClient.delete(deleteRequest)
            .thenApply(deleteResponse -> deleteResponse.result() == Result.Deleted);
    }
}
