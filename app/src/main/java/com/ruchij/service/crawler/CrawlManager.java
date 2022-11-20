package com.ruchij.service.crawler;

import com.ruchij.service.crawler.models.CrawledJob;
import io.reactivex.rxjava3.core.Flowable;

public interface CrawlManager {
    Flowable<CrawledJob> run(String userId);

    Flowable<CrawledJob> run(String userId, String linkedInEmail, String linkedInPassword);
}
