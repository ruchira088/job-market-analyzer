package com.ruchij.api.dao.job;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.ruchij.crawler.dao.job.ElasticsearchJobDao;
import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.utils.Transformers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.ruchij.crawler.dao.job.ElasticsearchJobDao.INDEX;

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
	public CompletableFuture<List<Job>> findByCrawlerTaskId(String crawlerTaskId, int pageSize, int pageNumber) {
		return this.elasticsearchJobDao.findByCrawlerTaskId(crawlerTaskId, pageSize, pageNumber);
	}

	@Override
	public CompletableFuture<List<Job>> search(String keyword, String crawlerTaskId, int pageSize, int pageNumber) {
		SearchRequest searchRequest = SearchRequest.of(builder ->
			builder
				.index(INDEX)
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
										matchQueryBuilder.field("crawlerTaskId").query(crawlerTaskId)
									)
									._toQuery()
							)
					)
				)
		);

		return this.elasticsearchAsyncClient.search(searchRequest, Job.class)
			.thenApply(Transformers::results);
	}

	@Override
	public CompletableFuture<Long> countJobsByCrawlerTaskId(String crawlerTaskId) {
		CountRequest countRequest = CountRequest.of(builder ->
			builder
				.index(INDEX)
				.query(queryBuilder ->
					queryBuilder.match(
						MatchQuery.of(matchQueryBuilder ->
							matchQueryBuilder.field("crawlerTaskId").query(crawlerTaskId)
						)
					)
				)
		);

		return this.elasticsearchAsyncClient.count(countRequest).thenApply(CountResponse::count);
	}
}
