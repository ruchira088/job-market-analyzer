package com.ruchij.crawler.service.crawler;

import com.ruchij.crawler.service.crawler.models.CrawledJob;
import io.reactivex.rxjava3.core.Flowable;

public interface CrawlManager {
    Flowable<CrawledJob> run(String userId, String linkedInEmail, String linkedInPassword);
}
