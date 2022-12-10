package com.ruchij.crawler.service.crawler;

import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;
import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.service.clock.Clock;
import com.ruchij.crawler.service.crawler.models.CrawledJob;
import com.ruchij.crawler.service.random.RandomGenerator;
import io.reactivex.rxjava3.core.Flowable;

import java.util.Optional;

public class CrawlManagerImpl implements CrawlManager {
    private final Crawler crawler;
    private final CrawlerTaskDao crawlerTaskDao;
    private final JobDao jobDao;
    private final RandomGenerator<String> idGenerator;
    private final Clock clock;

    public CrawlManagerImpl(
        Crawler crawler,
        CrawlerTaskDao crawlerTaskDao,
        JobDao jobDao,
        RandomGenerator<String> idGenerator,
        Clock clock
    ) {
        this.crawler = crawler;
        this.crawlerTaskDao = crawlerTaskDao;
        this.jobDao = jobDao;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public Flowable<CrawledJob> run(String userId, String linkedInEmail, String linkedInPassword) {
        String crawlerTaskId = idGenerator.generate();

        CrawlerTask crawlerTask =
            new CrawlerTask(crawlerTaskId, userId, clock.timestamp(), Optional.empty());

        return Flowable.fromCompletionStage(crawlerTaskDao.insert(crawlerTask))
            .concatMap(value -> crawler.crawl(crawlerTaskId, linkedInEmail, linkedInPassword))
            .concatMap(crawledJob -> Flowable.fromCompletionStage(jobDao.insert(crawledJob.job())).map(__ -> crawledJob))
            .concatWith(
                Flowable.fromCompletionStage(crawlerTaskDao.setFinishedTimestamp(crawlerTaskId, clock.timestamp()))
                    .concatMap(__ -> Flowable.empty())
            );
    }
}
