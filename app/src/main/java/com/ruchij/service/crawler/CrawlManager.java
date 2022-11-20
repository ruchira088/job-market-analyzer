package com.ruchij.service.crawler;

import com.ruchij.dao.task.models.CrawlerTask;

import java.util.concurrent.CompletableFuture;

public interface CrawlManager {
    CompletableFuture<CrawlerTask> run(String userId);

    CompletableFuture<CrawlerTask> run(String userId, String linkedInEmail, String linkedInPassword);
}
