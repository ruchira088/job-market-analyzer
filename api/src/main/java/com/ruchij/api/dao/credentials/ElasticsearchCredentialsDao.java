package com.ruchij.api.dao.credentials;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.ruchij.api.dao.credentials.models.Credentials;
import com.ruchij.crawler.utils.Kleisli;

import java.util.Optional;

public class ElasticsearchCredentialsDao implements CredentialsDao<Void> {
	private static final String INDEX = "credentials";
	private final ElasticsearchAsyncClient elasticsearchAsyncClient;

	public ElasticsearchCredentialsDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
		this.elasticsearchAsyncClient = elasticsearchAsyncClient;
	}

	@Override
	public Kleisli<Void, String> insert(Credentials credentials) {
		IndexRequest<Credentials> indexRequest =
			IndexRequest.of(builder -> builder.index(INDEX).id(credentials.userId()).document(credentials));

		return new Kleisli<Void, IndexResponse>(__ -> elasticsearchAsyncClient.index(indexRequest))
			.map(IndexResponse::id);
	}

	@Override
	public Kleisli<Void, Optional<Credentials>> findByUserId(String userId) {
		GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(userId));

		return new Kleisli<Void, GetResponse<Credentials>>(__ ->
			elasticsearchAsyncClient.get(getRequest, Credentials.class)
		)
			.map(getResponse -> Optional.ofNullable(getResponse.source()));
	}
}
