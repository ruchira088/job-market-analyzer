package com.ruchij.crawler.service.crawler.models;

import com.ruchij.crawler.dao.job.models.Job;

public record CrawledJob(String crawlId, Job job, int currentJobPosition, int allPages) {
    private static final int JOBS_PER_PAGE = 24;

    public int estimatedJobCount() {
        return allPages * JOBS_PER_PAGE;
    }
}
