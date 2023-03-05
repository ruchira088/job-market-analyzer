package com.ruchij.test;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.ruchij.crawler.utils.JsonUtils;
import com.ruchij.migration.containers.ElasticsearchContainer;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;

public interface ElasticsearchTest {
	static void run(ElasticsearchTest elasticsearchTest) throws Exception {
		try (ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer();
		     ElasticsearchClientBuilder elasticsearchClientBuilder =
			     new ElasticsearchClientBuilder(elasticsearchContainer.elasticsearchConfiguration(), new JacksonJsonpMapper(JsonUtils.objectMapper))) {
			ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();

			elasticsearchTest.run(elasticsearchAsyncClient);
		}
	}

	void run(ElasticsearchAsyncClient elasticsearchAsyncClient) throws Exception;
}
