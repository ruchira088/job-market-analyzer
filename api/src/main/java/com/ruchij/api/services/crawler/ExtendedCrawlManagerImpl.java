package com.ruchij.api.services.crawler;

import com.ruchij.api.exceptions.ResourceConflictException;
import com.ruchij.api.services.lock.LockService;
import com.ruchij.crawler.service.crawler.CrawlManager;
import com.ruchij.crawler.service.crawler.models.CrawledJob;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ExtendedCrawlManagerImpl implements ExtendedCrawlManager {
	private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(10);

	private static final Logger logger = LoggerFactory.getLogger(ExtendedCrawlManagerImpl.class);

	private final CrawlManager crawlManager;
	private final LockService lockService;
	private final LinkedInCredentialsService linkedInCredentialsService;

	public ExtendedCrawlManagerImpl(
		CrawlManager crawlManager,
		LockService lockService,
		LinkedInCredentialsService linkedInCredentialsService
	) {
		this.crawlManager = crawlManager;
		this.lockService = lockService;
		this.linkedInCredentialsService = linkedInCredentialsService;
	}

	@Override
	public Flowable<CrawledJob> runWithLock(String userId) {
		logger.info("Starting job crawl for id=%s".formatted(userId));

		return Flowable.fromCompletionStage(lockService.lock(userId, LOCK_TIMEOUT))
			.concatMap(maybeLock -> {
				if (maybeLock.isEmpty()) {
					ResourceConflictException resourceConflictException =
						new ResourceConflictException("Job crawl is already active for id=%s".formatted(userId));

					logger.warn("Crawl already active", resourceConflictException);
					return Flowable.error(resourceConflictException);
				} else {
					logger.info("Acquired crawl lock for id=%s".formatted(userId));

					return Flowable.fromCompletionStage(linkedInCredentialsService.getByUserId(userId));
				}
			})
			.concatMap(linkedInCredentials ->
				run(linkedInCredentials.userId(), linkedInCredentials.email(), linkedInCredentials.password())
			)
			.doFinally(() -> {
				lockService.release(userId);
				logger.info("Released crawl lock for id=%s".formatted(userId));
			});
	}

	@Override
	public Flowable<CrawledJob> run(String userId, String linkedInEmail, String linkedInPassword) {
		return crawlManager.run(userId, linkedInEmail, linkedInPassword);
	}
}
