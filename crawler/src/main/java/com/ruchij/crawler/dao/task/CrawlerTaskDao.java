package com.ruchij.crawler.dao.task;

import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.utils.Kleisli;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CrawlerTaskDao<A> {
	Kleisli<A, String> insert(CrawlerTask crawlerTask);

	Kleisli<A, Optional<CrawlerTask>> findById(String crawlerTaskId);

	Kleisli<A, Boolean> setFinishedTimestamp(String crawlerTaskId, Instant finishedTimestamp);

	Kleisli<A, List<CrawlerTask>> findByUserId(String userId, int pageSize, int pageNumber);
}
