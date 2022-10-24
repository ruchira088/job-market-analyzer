package com.ruchij;

import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.task.CrawlerTaskDao;
import com.ruchij.dao.task.models.CrawlerTask;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.random.RandomGenerator;
import io.reactivex.rxjava3.core.Flowable;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class CrawlTaskRunner {
    private final Crawler crawler;
    private final CrawlerTaskDao crawlerTaskDao;
    private final JobDao jobDao;
    private final Clock clock;
    private final RandomGenerator<String> idGenerator;

    public CrawlTaskRunner(
        Crawler crawler,
        CrawlerTaskDao crawlerTaskDao,
        JobDao jobDao,
        Clock clock,
        RandomGenerator<String> idGenerator
    ) {
        this.crawler = crawler;
        this.crawlerTaskDao = crawlerTaskDao;
        this.jobDao = jobDao;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    public CompletableFuture<CrawlerTask> run() {
        String crawlerTaskId = idGenerator.generate();

        CrawlerTask crawlerTask = new CrawlerTask();
        crawlerTask.setCrawlerId(crawlerTaskId);
        crawlerTask.setStartedAt(clock.timestamp());

        CompletableFuture<CrawlerTask> completableFuture = new CompletableFuture<>();

        Flowable.fromCompletionStage(crawlerTaskDao.insert(crawlerTask))
            .flatMap(value -> crawler.crawl(crawlerTaskId))
            .flatMap(crawledJob -> Flowable.fromCompletionStage(jobDao.insert(crawledJob.job())))
            .doFinally(() -> {
                Instant timestamp = clock.timestamp();

                crawlerTask.setFinishedAt(timestamp);
                crawlerTaskDao.setFinishedTimestamp(crawlerTaskId, timestamp)
                    .thenApplyAsync(result -> completableFuture.complete(crawlerTask));
            })
            .subscribe();

        return completableFuture;
    }
}
