package com.ruchij.api.services.authorization;

import com.ruchij.api.services.authorization.models.EntityType;
import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;

import java.util.concurrent.CompletableFuture;

public class AuthorizationServiceImpl implements AuthorizationService {
	private final CrawlerTaskDao crawlerTaskDao;
	private final JobDao jobDao;

	public AuthorizationServiceImpl(CrawlerTaskDao crawlerTaskDao, JobDao jobDao) {
		this.crawlerTaskDao = crawlerTaskDao;
		this.jobDao = jobDao;
	}

	@Override
	public CompletableFuture<Boolean> hasPermission(String userId, EntityType entityType, String entityId) {
		return switch (entityType) {
			case CRAWLER_TASK -> this.crawlerTaskDao.findById(entityId)
				.thenApply(maybeCrawlerTask ->
					maybeCrawlerTask.map(crawlerTask -> crawlerTask.userId().equals(userId)).orElse(false)
				);

			case JOB -> this.jobDao.findById(entityId)
				.thenApply(maybeJob -> maybeJob.map(Job::crawlerTaskId))
				.thenCompose(maybeCrawlerTaskId ->
					maybeCrawlerTaskId
						.map(crawlerTaskId -> hasPermission(userId, EntityType.CRAWLER_TASK, crawlerTaskId))
						.orElse(CompletableFuture.completedFuture(false))
				);
		};
	}
}
