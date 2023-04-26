package com.ruchij.crawler.dao.job;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.utils.Transformers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ElasticsearchJobDao implements JobDao {
	private static final String INDEX = "jobs";

	private final ElasticsearchAsyncClient elasticsearchAsyncClient;
	private final String indexName;

	public ElasticsearchJobDao(ElasticsearchAsyncClient elasticsearchAsyncClient, String indexPrefix) {
		this.elasticsearchAsyncClient = elasticsearchAsyncClient;
		this.indexName = "%s-%s".formatted(indexPrefix, INDEX);
	}

	public String getIndexName() {
		return indexName;
	}

	@Override
	public CompletableFuture<String> insert(Job job) {
		IndexRequest<Job> indexRequest =
			IndexRequest.of(builder -> builder.index(this.indexName).id(job.id()).document(job));

		return this.elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
	}

	@Override
	public CompletableFuture<Optional<Job>> findById(String jobId) {
		GetRequest getRequest = GetRequest.of(builder -> builder.index(this.indexName).id(jobId));

		return this.elasticsearchAsyncClient.get(getRequest, Job.class)
			.thenApply(jobGetResponse -> Optional.ofNullable(jobGetResponse.source()));
	}

	@Override
	public CompletableFuture<List<Job>> findByCrawlerTaskId(String crawlerTaskId, int pageSize, int pageNumber) {
		SearchRequest searchRequest = SearchRequest.of(builder ->
			builder
				.index(this.indexName)
				.size(pageSize)
				.from(pageSize * pageNumber)
				.query(queryBuilder ->
					queryBuilder.match(
						MatchQuery.of(matchQueryBuilder ->
							matchQueryBuilder.field("crawlerTaskId").query(crawlerTaskId)
						)
					)
				)
		);

		return this.elasticsearchAsyncClient.search(searchRequest, Job.class)
			.thenApply(Transformers::results);
	}
}
