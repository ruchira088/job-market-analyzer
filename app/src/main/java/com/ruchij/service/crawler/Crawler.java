package com.ruchij.service.crawler;

import com.ruchij.service.crawler.models.CrawlProgress;
import io.reactivex.rxjava3.core.Flowable;

public interface Crawler {
    Flowable<CrawlProgress> crawl();
}
