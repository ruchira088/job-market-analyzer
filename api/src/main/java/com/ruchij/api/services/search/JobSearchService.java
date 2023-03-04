package com.ruchij.api.services.search;

import com.ruchij.crawler.dao.job.models.Job;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface JobSearchService {
    CompletableFuture<List<Job>> findByCrawlerId(String crawlerId, int pageSize, int pageNumber);
}
