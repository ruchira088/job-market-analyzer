package com.ruchij.api.services.crawler;

import com.ruchij.api.exceptions.ResourceConflictException;
import com.ruchij.api.pubsub.publisher.Publisher;
import com.ruchij.api.pubsub.subscriber.Subscriber;
import com.ruchij.api.services.crawler.models.CrawledJobMessage;
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
	private static final String CRAWLED_JOB_TOPIC = "crawled-job";

	private static final Logger logger = LoggerFactory.getLogger(ExtendedCrawlManagerImpl.class);

	private final CrawlManager crawlManager;
	private final LockService lockService;
	private final LinkedInCredentialsService linkedInCredentialsService;
	private final Publisher publisher;
	private final Subscriber subscriber;

	public ExtendedCrawlManagerImpl(
		CrawlManager crawlManager,
		LockService lockService,
		LinkedInCredentialsService linkedInCredentialsService,
		Publisher publisher,
		Subscriber subscriber
	) {
		this.crawlManager = crawlManager;
		this.lockService = lockService;
		this.linkedInCredentialsService = linkedInCredentialsService;
		this.publisher = publisher;
		this.subscriber = subscriber;
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
			.concatMap(crawledJob ->
				Flowable.fromCompletionStage(
					publisher.publish(CRAWLED_JOB_TOPIC, new CrawledJobMessage(userId, crawledJob))
					)
					.map(__ -> crawledJob))
			.doFinally(() -> {
				lockService.release(userId);
				logger.info("Released crawl lock for id=%s".formatted(userId));
			});
	}

	@Override
	public Flowable<CrawledJob> run(String userId, String linkedInEmail, String linkedInPassword) {
		return crawlManager.run(userId, linkedInEmail, linkedInPassword);
	}

	@Override
	public Flowable<CrawledJob> listenToCrawledJobs(String userId) {
		return this.subscriber.subscribe(CRAWLED_JOB_TOPIC, CrawledJobMessage.class, "not-used")
			.filter(crawledJobMessage -> crawledJobMessage.userId().equals(userId))
			.map(CrawledJobMessage::crawledJob);
	}
}
