package com.ruchij.crawler.service.crawler;

import com.ruchij.crawler.service.crawler.models.CrawledJob;
import io.reactivex.rxjava3.core.Flowable;

public interface Crawler {
    Flowable<CrawledJob> crawl(String crawlerTaskId, String email, String password);
}
