package com.ruchij.crawler.dao.jdbi.models;

import com.ruchij.crawler.dao.task.models.CrawlerTask;

import java.time.Instant;
import java.util.Optional;

public class JdbiCrawlerTask {
	private String id;
	private String userId;
	private Instant startedAt;
	private Instant finishedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {
		this.startedAt = startedAt;
	}

	public Instant getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Instant finishedAt) {
		this.finishedAt = finishedAt;
	}

	public CrawlerTask crawlerTask() {
		return new CrawlerTask(id, userId, startedAt, Optional.ofNullable(finishedAt));
	}

	public static JdbiCrawlerTask from(CrawlerTask crawlerTask) {
		JdbiCrawlerTask jdbiCrawlerTask = new JdbiCrawlerTask();
		jdbiCrawlerTask.setId(crawlerTask.id());
		jdbiCrawlerTask.setUserId(crawlerTask.userId());
		jdbiCrawlerTask.setStartedAt(crawlerTask.startedAt());
		jdbiCrawlerTask.setFinishedAt(crawlerTask.finishedAt().orElse(null));

		return jdbiCrawlerTask;
	}
}
