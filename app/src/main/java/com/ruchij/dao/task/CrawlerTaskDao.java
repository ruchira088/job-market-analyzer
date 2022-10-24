package com.ruchij.dao.task;

import com.ruchij.dao.task.models.CrawlerTask;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CrawlerTaskDao {
    CompletableFuture<String> insert(CrawlerTask crawlerTask);

    CompletableFuture<Optional<CrawlerTask>> setFinishedTimestamp(String crawlerTaskId, Instant finishedTimestamp);
}
