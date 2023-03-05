package com.ruchij.crawler.dao.task;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.utils.Transformers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ElasticsearchCrawlerTaskDao implements CrawlerTaskDao {
	private static final String INDEX = "crawler_tasks";

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
					.id(crawlerTask.id())
					.document(crawlerTask)
			);

		return this.elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
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

		return this.elasticsearchAsyncClient.update(updateRequest, CrawlerTask.class)
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
	public CompletableFuture<List<CrawlerTask>> findByUserId(String userId, int pageSize, int pageNumber) {
		SearchRequest searchRequest = SearchRequest.of(searchQueryBuilder ->
			searchQueryBuilder
				.index(INDEX)
				.size(pageSize)
				.from(pageSize * pageNumber)
				.sort(SortOptions.of(sortOptionsBuilder ->
						sortOptionsBuilder.field(fieldSortBuilder ->
							fieldSortBuilder.field("startedAt").order(SortOrder.Desc)
						)
					)
				)
				.query(queryBuilder ->
					queryBuilder.match(MatchQuery.of(matchQueryBuilder ->
							matchQueryBuilder.field("userId").query(userId)
						)
					)
				)
		);

		return this.elasticsearchAsyncClient.search(searchRequest, CrawlerTask.class)
			.thenApply(Transformers::results);
	}

	@Override
	public CompletableFuture<Optional<CrawlerTask>> findById(String crawlerTaskId) {
		GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(crawlerTaskId));

		return this.elasticsearchAsyncClient.get(getRequest, CrawlerTask.class)
			.thenApply(crawlerTaskGetResponse -> Optional.ofNullable(crawlerTaskGetResponse.source()));
	}

	private record UpdateFinishedTimestamp(Instant finishedAt) {
	}
}
