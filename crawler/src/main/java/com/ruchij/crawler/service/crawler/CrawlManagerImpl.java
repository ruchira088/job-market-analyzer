package com.ruchij.crawler.service.crawler;

import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;
import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.dao.transaction.Transactor;
import com.ruchij.crawler.service.crawler.models.CrawledJob;
import com.ruchij.crawler.service.random.RandomGenerator;
import io.reactivex.rxjava3.core.Flowable;

import java.time.Clock;
import java.util.Optional;

public class CrawlManagerImpl<A> implements CrawlManager {
	private final Crawler crawler;
	private final CrawlerTaskDao<A> crawlerTaskDao;
	private final Transactor<A> transactor;
	private final JobDao jobDao;
	private final RandomGenerator<String> idGenerator;
	private final Clock clock;

	public CrawlManagerImpl(
		Crawler crawler,
		CrawlerTaskDao<A> crawlerTaskDao,
		Transactor<A> transactor,
		JobDao jobDao,
		RandomGenerator<String> idGenerator,
		Clock clock
	) {
		this.crawler = crawler;
		this.crawlerTaskDao = crawlerTaskDao;
		this.transactor = transactor;
		this.jobDao = jobDao;
		this.idGenerator = idGenerator;
		this.clock = clock;
	}

	@Override
	public Flowable<CrawledJob> run(String userId, String linkedInEmail, String linkedInPassword) {
		String crawlerTaskId = idGenerator.generate();

		CrawlerTask crawlerTask =
			new CrawlerTask(crawlerTaskId, userId, clock.instant(), Optional.empty());

		return Flowable.fromCompletionStage(transactor.transaction(crawlerTaskDao.insert(crawlerTask)))
			.concatMap(value -> crawler.crawl(crawlerTaskId, linkedInEmail, linkedInPassword))
			.concatMap(crawledJob -> Flowable.fromCompletionStage(jobDao.insert(crawledJob.job())).map(__ -> crawledJob))
			.concatWith(
				Flowable.defer(() ->
					Flowable.fromCompletionStage(
							transactor.transaction(
								crawlerTaskDao.setFinishedTimestamp(crawlerTaskId, clock.instant())
							)
						)
						.concatMap(__ -> Flowable.empty())
				)
			);
	}
}
