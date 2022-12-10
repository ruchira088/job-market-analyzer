package com.ruchij.api.services.crawler;

import com.ruchij.crawler.service.crawler.CrawlManager;
import com.ruchij.crawler.service.crawler.models.CrawledJob;
import io.reactivex.rxjava3.core.Flowable;

public interface ExtendedCrawlManager extends CrawlManager {
    Flowable<CrawledJob> runWithLock(String userId);
}
