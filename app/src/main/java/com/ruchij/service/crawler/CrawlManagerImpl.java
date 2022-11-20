package com.ruchij.service.crawler;

import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.task.CrawlerTaskDao;
import com.ruchij.dao.task.models.CrawlerTask;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.models.CrawledJob;
import com.ruchij.service.linkedin.LinkedInCredentialsService;
import com.ruchij.service.random.RandomGenerator;
import io.reactivex.rxjava3.core.Flowable;

public class CrawlManagerImpl implements CrawlManager {
    private final Crawler crawler;
    private final LinkedInCredentialsService linkedInCredentialsService;
    private final CrawlerTaskDao crawlerTaskDao;
    private final JobDao jobDao;
    private final RandomGenerator<String> idGenerator;
    private final Clock clock;

    public CrawlManagerImpl(
        Crawler crawler,
        LinkedInCredentialsService linkedInCredentialsService,
        CrawlerTaskDao crawlerTaskDao,
        JobDao jobDao,
        RandomGenerator<String> idGenerator,
        Clock clock
    ) {
        this.crawler = crawler;
        this.linkedInCredentialsService = linkedInCredentialsService;
        this.crawlerTaskDao = crawlerTaskDao;
        this.jobDao = jobDao;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public Flowable<CrawledJob> run(String userId) {
        return Flowable.fromCompletionStage(linkedInCredentialsService.getByUserId(userId))
            .flatMap(linkedInCredentials ->
                run(linkedInCredentials.userId(), linkedInCredentials.email(), linkedInCredentials.password())
            );
    }

    @Override
    public Flowable<CrawledJob> run(String userId, String linkedInEmail, String linkedInPassword) {
        String crawlerTaskId = idGenerator.generate();

        CrawlerTask crawlerTask = new CrawlerTask();
        crawlerTask.setCrawlerId(crawlerTaskId);
        crawlerTask.setUserId(userId);
        crawlerTask.setStartedAt(clock.timestamp());

        return Flowable.fromCompletionStage(crawlerTaskDao.insert(crawlerTask))
            .concatMap(value -> crawler.crawl(crawlerTaskId, linkedInEmail, linkedInPassword))
            .concatMap(crawledJob -> Flowable.fromCompletionStage(jobDao.insert(crawledJob.job())).map(__ -> crawledJob))
            .concatWith(
                Flowable.fromCompletionStage(crawlerTaskDao.setFinishedTimestamp(crawlerTaskId, clock.timestamp()))
                    .concatMap(__ -> Flowable.empty())
            );
    }
}
