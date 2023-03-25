package com.ruchij.api.dao.keyword;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.ruchij.api.dao.keyword.models.Keyword;
import com.ruchij.crawler.utils.Transformers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ElasticsearchKeywordDao implements KeywordDao {
	private static final String INDEX = "keyword";
	private final ElasticsearchAsyncClient elasticsearchAsyncClient;

	public ElasticsearchKeywordDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
		this.elasticsearchAsyncClient = elasticsearchAsyncClient;
	}

	@Override
	public CompletableFuture<String> insert(Keyword keyword) {
		IndexRequest<Keyword> indexRequest =
			IndexRequest.of(builder ->
				builder.index(INDEX).id(keyword.id()).document(keyword)
			);

		return this.elasticsearchAsyncClient.index(indexRequest).thenApply(WriteResponseBase::id);
	}

	@Override
	public CompletableFuture<List<Keyword>> getByUserId(String userId, int pageSize, int pageNumber) {
		SearchRequest searchRequest = SearchRequest.of(builder ->
			builder
				.index(INDEX)
				.size(pageSize)
				.from(pageSize * pageNumber)
				.sort(SortOptions.of(sortOptionsBuilder ->
						sortOptionsBuilder.field(fieldSortBuilder ->
							fieldSortBuilder.field("createdAt").order(SortOrder.Desc)
						)
					)
				)
				.query(queryBuilder ->
					queryBuilder.match(
						MatchQuery.of(matchQueryBuilder ->
							matchQueryBuilder.field("userId").query(userId)
						)
					)
				)
		);

		return this.elasticsearchAsyncClient.search(searchRequest, Keyword.class)
			.thenApply(Transformers::results);
	}
}
