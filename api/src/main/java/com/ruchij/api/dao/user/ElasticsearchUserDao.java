package com.ruchij.api.dao.user;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.*;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.Transformers;

import java.util.Optional;

public class ElasticsearchUserDao implements UserDao<Void> {
	private static final String INDEX = "users";

	private final ElasticsearchAsyncClient elasticsearchAsyncClient;
	private final String indexName;

	public ElasticsearchUserDao(ElasticsearchAsyncClient elasticsearchAsyncClient, String indexPrefix) {
		this.elasticsearchAsyncClient = elasticsearchAsyncClient;
		this.indexName = "%s-%s".formatted(indexPrefix, INDEX);
	}

	@Override
	public Kleisli<Void, String> insert(User user) {
		IndexRequest<User> indexRequest =
			IndexRequest.of(builder -> builder.index(this.indexName).id(user.id()).document(user));

		return new Kleisli<Void, IndexResponse>(__ -> elasticsearchAsyncClient.index(indexRequest))
			.map(WriteResponseBase::id);
	}

	@Override
	public Kleisli<Void, Optional<User>> findById(String userId) {
		GetRequest getRequest = GetRequest.of(builder -> builder.index(this.indexName).id(userId));

		return new Kleisli<Void, GetResponse<User>>(__ -> elasticsearchAsyncClient.get(getRequest, User.class))
			.map(getResponse -> Optional.ofNullable(getResponse.source()));
	}

	@Override
	public Kleisli<Void, Optional<User>> findByEmail(String email) {
		SearchRequest searchRequest = SearchRequest.of(builder ->
			builder.index(this.indexName)
				.query(queryBuilder ->
					queryBuilder.match(
						MatchQuery.of(matchQueryBuilder ->
							matchQueryBuilder.field("email").query(email)
						)
					)
				)
				.size(1)
		);

		return new Kleisli<Void, SearchResponse<User>>(__ -> elasticsearchAsyncClient.search(searchRequest, User.class))
			.map(Transformers::findFirst);
	}
}
