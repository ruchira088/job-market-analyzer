package com.ruchij.api.services.authorization;

import com.ruchij.api.services.authorization.models.EntityType;
import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;
import com.ruchij.crawler.dao.transaction.Transactor;

import java.util.concurrent.CompletableFuture;

public class AuthorizationServiceImpl<A> implements AuthorizationService {
	private final CrawlerTaskDao<A> crawlerTaskDao;
	private final Transactor<A> transactor;
	private final JobDao jobDao;

	public AuthorizationServiceImpl(CrawlerTaskDao<A> crawlerTaskDao, Transactor<A> transactor, JobDao jobDao) {
		this.crawlerTaskDao = crawlerTaskDao;
		this.transactor = transactor;
		this.jobDao = jobDao;
	}

	@Override
	public CompletableFuture<Boolean> hasPermission(String userId, EntityType entityType, String entityId) {
		return switch (entityType) {
			case CRAWLER_TASK -> this.transactor.transaction(this.crawlerTaskDao.findById(entityId))
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
