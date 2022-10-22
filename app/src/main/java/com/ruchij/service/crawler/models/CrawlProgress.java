package com.ruchij.service.crawler.models;

import java.time.Instant;

public record CrawlProgress(String crawlId, Instant timestamp, int currentJobPosition, int allPages) {
    private static final int JOBS_PER_PAGE = 24;

    public int estimatedJobCount() {
        return allPages * JOBS_PER_PAGE;
    }
}
