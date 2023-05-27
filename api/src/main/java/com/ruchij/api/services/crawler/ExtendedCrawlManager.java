package com.ruchij.api.services.crawler;

import com.ruchij.crawler.service.crawler.CrawlManager;
import com.ruchij.crawler.service.crawler.models.CrawledJob;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ExtendedCrawlManager extends CrawlManager {
	Flowable<CrawledJob> runWithLock(String userId);

	Flowable<CrawledJob> listenToCrawledJobs(String userId);

	Disposable triggerRunWithLock(String userId);

	CompletableFuture<String> stopUserCrawl(String userId);
}