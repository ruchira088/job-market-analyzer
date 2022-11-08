package com.ruchij.dao.task.models;

import java.time.Instant;
import java.util.Objects;

public class CrawlerTask {
    private String crawlerId;
    private String userId;
    private Instant startedAt;
    private Instant finishedAt;

    public String getCrawlerId() {
        return crawlerId;
    }

    public void setCrawlerId(String crawlerId) {
        this.crawlerId = crawlerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlerTask that = (CrawlerTask) o;
        return crawlerId.equals(that.crawlerId) && userId.equals(that.userId) && startedAt.equals(that.startedAt) && Objects.equals(finishedAt, that.finishedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crawlerId, userId, startedAt, finishedAt);
    }
}
