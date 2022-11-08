package com.ruchij.service.crawler;

import com.ruchij.service.crawler.models.CrawledJob;
import io.reactivex.rxjava3.core.Flowable;

public interface Crawler {
    Flowable<CrawledJob> crawl(String crawlerTaskId, String email, String password);
}
