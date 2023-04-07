package com.ruchij.crawler.dao.linkedin;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch.core.*;
import com.ruchij.crawler.dao.linkedin.models.EncryptedLinkedInCredentials;
import com.ruchij.crawler.utils.Kleisli;
import com.ruchij.crawler.utils.Transformers;

import java.util.List;
import java.util.Optional;

public class ElasticsearchEncryptedLinkedInCredentialsDao implements EncryptedLinkedInCredentialsDao<Void> {
	private static final String INDEX = "linkedin_credentials";

	private final ElasticsearchAsyncClient elasticsearchAsyncClient;

	public ElasticsearchEncryptedLinkedInCredentialsDao(ElasticsearchAsyncClient elasticsearchAsyncClient) {
		this.elasticsearchAsyncClient = elasticsearchAsyncClient;
	}

	@Override
	public Kleisli<Void, String> insert(EncryptedLinkedInCredentials encryptedLinkedInCredentials) {
		IndexRequest<EncryptedLinkedInCredentials> indexRequest =
			IndexRequest.of(builder ->
				builder
					.index(INDEX)
					.id(encryptedLinkedInCredentials.userId())
					.document(encryptedLinkedInCredentials)
			);

		return new Kleisli<Void, IndexResponse>(__ -> elasticsearchAsyncClient.index(indexRequest))
			.map(WriteResponseBase::id);
	}

	@Override
	public Kleisli<Void, List<EncryptedLinkedInCredentials>> getAll(int pageNumber, int pageSize) {
		SearchRequest searchRequest = SearchRequest.of(builder -> builder.index(INDEX).size(pageSize).from(pageNumber * pageSize));

		return new Kleisli<Void, SearchResponse<EncryptedLinkedInCredentials>>(__ ->
			elasticsearchAsyncClient.search(searchRequest, EncryptedLinkedInCredentials.class)
		)
			.map(Transformers::results);
	}

	@Override
	public Kleisli<Void, Optional<EncryptedLinkedInCredentials>> findByUserId(String userId) {
		GetRequest getRequest = GetRequest.of(builder -> builder.index(INDEX).id(userId));

		return new Kleisli<Void, GetResponse<EncryptedLinkedInCredentials>>(__ -> elasticsearchAsyncClient.get(getRequest, EncryptedLinkedInCredentials.class))
			.map(linkedInCredentialsGetResponse -> Optional.ofNullable(linkedInCredentialsGetResponse.source()));
	}

	@Override
	public Kleisli<Void, Boolean> deleteByUserId(String userId) {
		DeleteRequest deleteRequest = DeleteRequest.of(builder -> builder.index(INDEX).id(userId));

		return new Kleisli<Void, DeleteResponse>(__ -> elasticsearchAsyncClient.delete(deleteRequest))
			.map(deleteResponse -> deleteResponse.result() == Result.Deleted);
	}
}
