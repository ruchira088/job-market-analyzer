package com.ruchij;

import com.ruchij.dao.elasticsearch.models.EncryptedText;
import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.linkedin.LinkedInCredentialsDao;
import com.ruchij.dao.task.CrawlerTaskDao;
import com.ruchij.dao.task.models.CrawlerTask;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.encryption.EncryptionService;
import com.ruchij.service.random.RandomGenerator;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlTaskRunner {
    private final Crawler crawler;
    private final CrawlerTaskDao crawlerTaskDao;
    private final JobDao jobDao;
    private final LinkedInCredentialsDao linkedInCredentialsDao;
    private final EncryptionService encryptionService;
    private final Clock clock;
    private final RandomGenerator<String> idGenerator;

    public CrawlTaskRunner(
        Crawler crawler,
        CrawlerTaskDao crawlerTaskDao,
        JobDao jobDao,
        LinkedInCredentialsDao linkedInCredentialsDao,
        EncryptionService encryptionService,
        Clock clock,
        RandomGenerator<String> idGenerator
    ) {
        this.crawler = crawler;
        this.crawlerTaskDao = crawlerTaskDao;
        this.jobDao = jobDao;
        this.linkedInCredentialsDao = linkedInCredentialsDao;
        this.encryptionService = encryptionService;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        linkedInCredentialsDao.getAll()
            .flatMap(linkedInCredentials -> {
                    String email = decrypt(linkedInCredentials.getEmail());
                    String password = decrypt(linkedInCredentials.getPassword());

                    return Flowable.fromCompletionStage(run(linkedInCredentials.getUserId(), email, password))
                        .subscribeOn(Schedulers.from(executorService));
                }
            )
            .blockingSubscribe();
    }

    private String decrypt(EncryptedText encryptedText) throws GeneralSecurityException {
        byte[] decryptedBytes = encryptionService.decrypt(encryptedText.value());
        return new String(decryptedBytes);
    }

    private CompletableFuture<CrawlerTask> run(String userId, String email, String password) {
        String crawlerTaskId = idGenerator.generate();

        CrawlerTask crawlerTask = new CrawlerTask();
        crawlerTask.setCrawlerId(crawlerTaskId);
        crawlerTask.setUserId(userId);
        crawlerTask.setStartedAt(clock.timestamp());

        CompletableFuture<CrawlerTask> completableFuture = new CompletableFuture<>();

        Flowable.fromCompletionStage(crawlerTaskDao.insert(crawlerTask))
            .concatMap(value -> crawler.crawl(crawlerTaskId, email, password))
            .concatMap(crawledJob -> Flowable.fromCompletionStage(jobDao.insert(crawledJob.job())))
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
