package com.ruchij.service.crawler;

import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.task.CrawlerTaskDao;
import com.ruchij.dao.task.models.CrawlerTask;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.random.RandomGenerator;
import io.reactivex.rxjava3.core.Flowable;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

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
    public CompletableFuture<CrawlerTask> run(String userId) {
        return null;
    }

    @Override
    public CompletableFuture<CrawlerTask> run(String userId, String linkedInEmail, String linkedInPassword) {
        String crawlerTaskId = idGenerator.generate();

        CrawlerTask crawlerTask = new CrawlerTask();
        crawlerTask.setCrawlerId(crawlerTaskId);
        crawlerTask.setUserId(userId);
        crawlerTask.setStartedAt(clock.timestamp());

        CompletableFuture<CrawlerTask> completableFuture = new CompletableFuture<>();

        Flowable.fromCompletionStage(crawlerTaskDao.insert(crawlerTask))
            .concatMap(value -> crawler.crawl(crawlerTaskId, linkedInEmail, linkedInPassword))
            .concatMap(crawledJob -> Flowable.fromCompletionStage(jobDao.insert(crawledJob.job())))
            .doFinally(() -> {
                Instant timestamp = clock.timestamp();

                crawlerTask.setFinishedAt(timestamp);
                crawlerTaskDao.setFinishedTimestamp(crawlerTaskId, timestamp)
                    .thenApply(result -> completableFuture.complete(crawlerTask));
            })
            .subscribe();

        return completableFuture;
    }
}
