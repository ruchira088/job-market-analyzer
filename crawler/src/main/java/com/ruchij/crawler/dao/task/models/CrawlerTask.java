package com.ruchij.crawler.dao.task.models;

import java.time.Instant;
import java.util.Optional;

public record CrawlerTask(String id, String userId, Instant startedAt, Optional<Instant> finishedAt) {
}