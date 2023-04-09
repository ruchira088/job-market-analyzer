package com.ruchij.crawler.service.crawler;

import com.ruchij.crawler.service.crawler.models.CrawledJob;
import io.reactivex.rxjava3.core.Flowable;

import java.util.concurrent.CompletableFuture;

public interface Crawler {
	Flowable<CrawledJob> crawl(String crawlerTaskId, String email, String password);

	CompletableFuture<Boolean> verifyCredentials(String email, String password);

	CompletableFuture<Boolean> isHealthy();
}
