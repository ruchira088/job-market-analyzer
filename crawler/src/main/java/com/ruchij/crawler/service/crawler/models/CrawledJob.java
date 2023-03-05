package com.ruchij.crawler.service.crawler.models;

import com.ruchij.crawler.dao.job.models.Job;

public record CrawledJob(String crawlerTaskId, LinkedInJob linkedInJob, int position, int progress) {
    public Job job() {
        Job job = new Job();

        job.setId(linkedInJob.id());
        job.setCrawlerTaskId(crawlerTaskId);
        job.setCrawledAt(linkedInJob.crawledAt());
        job.setPosition(position);
        job.setLink(linkedInJob.link());
        job.setTitle(linkedInJob.title());
        job.setCompanyName(linkedInJob.companyName());
        job.setLocation(linkedInJob.location());
        job.setWorkplaceType(linkedInJob.workplaceType());
        job.setDetails(linkedInJob.details());

        return job;
    }
}
