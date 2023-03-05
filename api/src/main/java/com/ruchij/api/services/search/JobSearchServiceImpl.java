package com.ruchij.api.services.search;

import com.ruchij.api.dao.job.SearchableJobDao;
import com.ruchij.crawler.dao.job.models.Job;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JobSearchServiceImpl implements JobSearchService {
    private final SearchableJobDao searchableJobDao;

    public JobSearchServiceImpl(SearchableJobDao searchableJobDao) {
        this.searchableJobDao = searchableJobDao;
    }

    @Override
    public CompletableFuture<List<Job>> findByCrawlerTaskId(String crawlerTaskId, int pageSize, int pageNumber) {
        return this.searchableJobDao.findByCrawlerTaskId(crawlerTaskId, pageSize, pageNumber);
    }
}
