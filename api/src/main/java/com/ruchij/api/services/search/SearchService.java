package com.ruchij.api.services.search;

import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.dao.task.models.CrawlerTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SearchService {
	CompletableFuture<List<Job>> findJobsByCrawlerTaskId(String crawlerTaskId, int pageSize, int pageNumber);

	CompletableFuture<Long> countJobsByCrawlerTaskId(String crawlerTaskId);

	CompletableFuture<List<CrawlerTask>> findCrawlerTasksByUserId(String userId, int pageSize, int pageNumber);

	CompletableFuture<Job> getJobById(String jobId);
}
