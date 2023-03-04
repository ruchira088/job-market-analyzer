package com.ruchij.api.dao.job;

import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.job.models.Job;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SearchableJobDao extends JobDao {
    CompletableFuture<List<Job>> search(String keyword, String crawlerId, int pageSize, int pageNumber);
}
