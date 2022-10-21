package com.ruchij.dao.job;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.ruchij.dao.job.models.Job;

import java.util.concurrent.CompletableFuture;

public class ElasticsearchJobDao implements JobDao {
    private static final String INDEX = "jobs";
    private final ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchJobDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
    }

    @Override
    public CompletableFuture<String> insert(Job job) {
        IndexRequest<Job> indexRequest =
            IndexRequest.of(builder -> builder.index(INDEX).document(job));

        return elasticsearchAsyncClient.index(indexRequest).thenApplyAsync(WriteResponseBase::id);
    }
}
