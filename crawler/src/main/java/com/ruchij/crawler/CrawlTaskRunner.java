package com.ruchij.crawler;

import com.ruchij.crawler.service.crawler.CrawlManager;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlTaskRunner {
	private final CrawlManager crawlManager;
	private final LinkedInCredentialsService linkedInCredentialsService;

	public CrawlTaskRunner(
		CrawlManager crawlManager,
		LinkedInCredentialsService linkedInCredentialsService
	) {
		this.crawlManager = crawlManager;
		this.linkedInCredentialsService = linkedInCredentialsService;
	}

	public void run() {
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		linkedInCredentialsService.getAll()
			.flatMap(linkedInCredentials ->
				crawlManager.run(
						linkedInCredentials.userId(),
						linkedInCredentials.email(),
						linkedInCredentials.password()
					)
					.subscribeOn(Schedulers.from(executorService))
			)
			.blockingSubscribe();
	}
}
