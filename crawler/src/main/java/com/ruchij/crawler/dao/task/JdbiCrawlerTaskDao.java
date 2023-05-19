package com.ruchij.crawler.dao.task;

import com.ruchij.crawler.dao.jdbi.models.JdbiCrawlerTask;
import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.utils.Kleisli;
import org.jdbi.v3.core.Handle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbiCrawlerTaskDao implements CrawlerTaskDao<Handle> {
	private static final String SQL_INSERT =
		"INSERT INTO crawler_task(id, user_id, started_at, finished_at) VALUES(:id, :userId, :startedAt, :finishedAt)";

	private static final String SQL_SELECT = "SELECT id, user_id, started_at, finished_at FROM crawler_task";

	@Override
	public Kleisli<Handle, String> insert(CrawlerTask crawlerTask) {
		return new Kleisli<Handle, Integer>(handle ->
			CompletableFuture.completedFuture(
				handle.createUpdate(SQL_INSERT).bindBean(JdbiCrawlerTask.from(crawlerTask)).execute()
			)
		).as(crawlerTask.id());
	}

	@Override
	public Kleisli<Handle, Optional<CrawlerTask>> findById(String crawlerTaskId) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE id = :id".formatted(SQL_SELECT))
					.bind("id", crawlerTaskId)
					.mapToBean(JdbiCrawlerTask.class)
					.findOne()
					.map(JdbiCrawlerTask::crawlerTask)
			)
		);
	}

	@Override
	public Kleisli<Handle, Boolean> setFinishedTimestamp(String crawlerTaskId, Instant finishedTimestamp) {
		return new Kleisli<Handle, Integer>(handle ->
			CompletableFuture.completedFuture(
				handle.createUpdate("UPDATE crawler_task SET finished_at = :finishedAt WHERE id = :id")
					.bind("finishedAt", finishedTimestamp)
					.bind("id", crawlerTaskId)
					.execute()
			)
		).map(count -> count > 0);
	}

	@Override
	public Kleisli<Handle, List<CrawlerTask>> findByUserId(String userId, int pageSize, int pageNumber) {
		return new Kleisli<>(handle ->
			CompletableFuture.completedFuture(
				handle.createQuery("%s WHERE user_id = :userId LIMIT :limit OFFSET :offset".formatted(SQL_SELECT))
					.bind("userId", userId)
					.bind("limit", pageSize)
					.bind("offset", pageNumber * pageSize)
					.mapToBean(JdbiCrawlerTask.class)
					.map(JdbiCrawlerTask::crawlerTask)
					.list()
			)
		);
	}
}
