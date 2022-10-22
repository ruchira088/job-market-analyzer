package com.ruchij.dao.job.models;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public class Job {
    private String id;
    private String crawlId;
    private Instant crawledAt;
    private URL link;
    private String title;
    private String companyName;
    private String location;
    private Optional<WorkplaceType> workplaceType;
    private String details;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrawlId() {
        return crawlId;
    }

    public void setCrawlId(String crawlId) {
        this.crawlId = crawlId;
    }

    public Instant getCrawledAt() {
        return crawledAt;
    }

    public void setCrawledAt(Instant crawledAt) {
        this.crawledAt = crawledAt;
    }

    public URL getLink() {
        return link;
    }

    public void setLink(URL link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Optional<WorkplaceType> getWorkplaceType() {
        return workplaceType;
    }

    public void setWorkplaceType(Optional<WorkplaceType> workplaceType) {
        this.workplaceType = workplaceType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Job{" +
            "link=" + link +
            ", title='" + title + '\'' +
            ", companyName='" + companyName + '\'' +
            ", location='" + location + '\'' +
            ", workplaceType=" + workplaceType +
            ", details='" + details + '\'' +
            '}';
    }
}
