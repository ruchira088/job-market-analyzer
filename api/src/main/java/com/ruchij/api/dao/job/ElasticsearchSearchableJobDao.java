package com.ruchij.api.dao.job;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ruchij.crawler.dao.job.ElasticsearchJobDao;
import com.ruchij.crawler.dao.job.models.Job;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ElasticsearchSearchableJobDao implements SearchableJobDao {
    private final ElasticsearchAsyncClient elasticsearchAsyncClient;
    private final ElasticsearchJobDao elasticsearchJobDao;

    public ElasticsearchSearchableJobDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
        this.elasticsearchAsyncClient = elasticsearchAsyncClient;
        this.elasticsearchJobDao = new ElasticsearchJobDao(elasticsearchAsyncClient);
    }

    @Override
    public CompletableFuture<String> insert(Job job) {
        return this.elasticsearchJobDao.insert(job);
    }

    @Override
    public CompletableFuture<Optional<Job>> findById(String jobId) {
        return this.elasticsearchJobDao.findById(jobId);
    }

    @Override
    public CompletableFuture<List<Job>> findByCrawlerId(String crawlerId, int pageSize, int pageNumber) {
        return this.elasticsearchJobDao.findByCrawlerId(crawlerId, pageSize, pageNumber);
    }

    @Override
    public CompletableFuture<List<Job>> search(String keyword, String crawlerId, int pageSize, int pageNumber) {
        SearchRequest searchRequest = SearchRequest.of(builder ->
            builder
                .index(ElasticsearchJobDao.INDEX)
                .from(pageNumber * pageSize)
                .size(pageSize)
                .query(queryBuilder ->
                    queryBuilder.bool(booleanQueryBuilder ->
                        booleanQueryBuilder
                            .must(
                                MatchQuery.of(matchQueryBuilder ->
                                        matchQueryBuilder.field("details").query(keyword)
                                    )
                                    ._toQuery()
                            )
                            .must(
                                MatchQuery.of(matchQueryBuilder ->
                                        matchQueryBuilder.field("crawlerId").query(crawlerId)
                                    )
                                    ._toQuery()
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
