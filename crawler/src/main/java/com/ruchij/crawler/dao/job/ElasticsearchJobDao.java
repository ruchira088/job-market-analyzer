package com.ruchij.crawler.dao.job;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ruchij.crawler.dao.job.models.Job;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ElasticsearchJobDao implements JobDao {
    public static final String INDEX = "jobs";

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;

    public ElasticsearchJobDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
    }

    @Override
    public CompletableFuture<String> insert(Job job) {
        IndexRequest<Job> indexRequest =
            IndexRequest.of(builder -> builder.index(INDEX).document(job));

        return this.elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
    }

    @Override
    public CompletableFuture<Optional<Job>> findById(String jobId) {
        GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(jobId));

        return this.elasticsearchAsyncClient.get(getRequest, Job.class)
            .thenApply(jobGetResponse -> Optional.ofNullable(jobGetResponse.source()));
    }

    @Override
    public CompletableFuture<List<Job>> findByCrawlerTaskId(String crawlerTaskId, int pageSize, int pageNumber) {
        SearchRequest searchRequest = SearchRequest.of(builder ->
            builder
                .index(INDEX)
                .size(pageSize)
                .from(pageSize * pageNumber)
                .query(queryBuilder ->
                    queryBuilder.match(
                        MatchQuery.of(matchQueryBuilder ->
                            matchQueryBuilder.field("id").query(crawlerTaskId)
                        )
                    )
                )
        );

        return this.elasticsearchAsyncClient.search(searchRequest, Job.class)
            .thenApply(searchResponse ->
                searchResponse.hits().hits().stream().map(Hit::source).collect(Collectors.toList())
            );
    }
}
