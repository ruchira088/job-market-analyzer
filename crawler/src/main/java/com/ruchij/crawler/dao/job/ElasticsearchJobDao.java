package com.ruchij.crawler.dao.job;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.ruchij.crawler.dao.job.models.Job;

import java.util.Optional;
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

        return elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
    }

    @Override
    public CompletableFuture<Optional<Job>> findById(String jobId) {
        GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(jobId));

        return elasticsearchAsyncClient.get(getRequest, Job.class)
            .thenApply(jobGetResponse -> Optional.ofNullable(jobGetResponse.source()));
    }
}
