package com.ruchij.crawler.dao.task;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.*;
import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.Transformers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ElasticsearchCrawlerTaskDao implements CrawlerTaskDao<Void> {
	private static final String INDEX = "crawler_tasks";

	private final ElasticsearchAsyncClient elasticsearchAsyncClient;

	public ElasticsearchCrawlerTaskDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
		this.elasticsearchAsyncClient = elasticsearchAsyncClient;
	}

	@Override
	public Kleisli<Void, String> insert(CrawlerTask crawlerTask) {
		IndexRequest<CrawlerTask> indexRequest =
			IndexRequest.of(builder ->
				builder
					.index(INDEX)
					.refresh(Refresh.True)
					.id(crawlerTask.id())
					.document(crawlerTask)
			);

		return new Kleisli<Void, IndexResponse>(__ -> this.elasticsearchAsyncClient.index(indexRequest))
			.map(WriteResponseBase::id);
	}

	@Override
	public Kleisli<Void, Boolean> setFinishedTimestamp(String crawlerTaskId, Instant finishedTimestamp) {
		UpdateRequest<CrawlerTask, UpdateFinishedTimestamp> updateRequest =
			UpdateRequest.of(builder ->
				builder
					.index(INDEX)
					.id(crawlerTaskId)
					.refresh(Refresh.True)
					.doc(new UpdateFinishedTimestamp(finishedTimestamp))
			);

		return new Kleisli<>(__ -> this.elasticsearchAsyncClient.update(updateRequest, CrawlerTask.class)
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
			)
		);
	}

	@Override
	public Kleisli<Void, List<CrawlerTask>> findByUserId(String userId, int pageSize, int pageNumber) {
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

		return new Kleisli<Void, SearchResponse<CrawlerTask>>(__ -> this.elasticsearchAsyncClient.search(searchRequest, CrawlerTask.class))
			.map(Transformers::results);
	}

	@Override
	public Kleisli<Void, Optional<CrawlerTask>> findById(String crawlerTaskId) {
		GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(crawlerTaskId));

		return new Kleisli<Void, GetResponse<CrawlerTask>>(__ -> this.elasticsearchAsyncClient.get(getRequest, CrawlerTask.class))
			.map(crawlerTaskGetResponse -> Optional.ofNullable(crawlerTaskGetResponse.source()));
	}

	private record UpdateFinishedTimestamp(Instant finishedAt) {
	}
}
