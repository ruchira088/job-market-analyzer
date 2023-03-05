package com.ruchij.crawler.dao.task;

import com.ruchij.crawler.dao.task.models.CrawlerTask;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CrawlerTaskDao {
	CompletableFuture<String> insert(CrawlerTask crawlerTask);

	CompletableFuture<Optional<CrawlerTask>> findById(String crawlerTaskId);

	CompletableFuture<Boolean> setFinishedTimestamp(String crawlerTaskId, Instant finishedTimestamp);

	CompletableFuture<List<CrawlerTask>> findByUserId(String userId, int pageSize, int pageNumber);
}
