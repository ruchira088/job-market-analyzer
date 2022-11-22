package com.ruchij.dao.task;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import com.ruchij.dao.task.models.CrawlerTask;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ElasticsearchCrawlerTaskDao implements CrawlerTaskDao {
    private static final String INDEX = "crawler_jobs";

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchCrawlerTaskDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
    }

    @Override
    public CompletableFuture<String> insert(CrawlerTask crawlerTask) {
        IndexRequest<CrawlerTask> indexRequest =
            IndexRequest.of(builder ->
                builder
                    .index(INDEX)
                    .refresh(Refresh.True)
                    .id(crawlerTask.getCrawlerId())
                    .document(crawlerTask)
            );

        return elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
    }

    @Override
    public CompletableFuture<Boolean> setFinishedTimestamp(String crawlerTaskId, Instant finishedTimestamp) {
        UpdateRequest<CrawlerTask, UpdateFinishedTimestamp> updateRequest =
            UpdateRequest.of(builder ->
                builder
                    .index(INDEX)
                    .id(crawlerTaskId)
                    .refresh(Refresh.True)
                    .doc(new UpdateFinishedTimestamp(finishedTimestamp))
            );

        return elasticsearchAsyncClient.update(updateRequest, CrawlerTask.class)
            .thenApply(crawlerTaskUpdateResponse -> true)
            .exceptionallyCompose(throwable ->
                Optional.ofNullable(throwable.getCause())
                    .flatMap(cause -> {
                        if (cause instanceof ElasticsearchException) {
                            return Optional.of((ElasticsearchException) cause);
                        } else {
                            return Optional.empty();
                        }
                    })
                    .flatMap(elasticsearchException -> Optional.ofNullable(elasticsearchException.error().type()))
                    .filter(errorType -> errorType.equalsIgnoreCase("document_missing_exception"))
                    .map(value -> CompletableFuture.completedFuture(false))
                    .orElse(CompletableFuture.failedFuture(throwable))
            );
    }

    @Override
    public CompletableFuture<Optional<CrawlerTask>> findById(String crawlerTaskId) {
        GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(crawlerTaskId));

        return elasticsearchAsyncClient.get(getRequest, CrawlerTask.class)
            .thenApply(crawlerTaskGetResponse -> Optional.ofNullable(crawlerTaskGetResponse.source()));
    }

    private class UpdateFinishedTimestamp {
        private Instant finishedAt;

        public UpdateFinishedTimestamp() {
        }

        private UpdateFinishedTimestamp(Instant finishedAt) {
            this.finishedAt = finishedAt;
        }

        public Instant getFinishedAt() {
            return finishedAt;
        }

        public void setFinishedAt(Instant finishedAt) {
            this.finishedAt = finishedAt;
        }
    }
}
