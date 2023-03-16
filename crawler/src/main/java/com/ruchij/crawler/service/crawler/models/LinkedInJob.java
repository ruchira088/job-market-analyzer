package com.ruchij.crawler.service.crawler.models;

import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.dao.job.models.WorkplaceType;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public record LinkedInJob(String id, Instant crawledAt, URL link, String title, String companyName, String companyLogoUrl,
                          String location, Optional<WorkplaceType> workplaceType, String details) {
	public Job job(String jobId, String crawlerTaskId, int position) {
		Job job = new Job();

		job.setId(jobId);
		job.setLinkedInId(id);
		job.setCrawlerTaskId(crawlerTaskId);
		job.setCrawledAt(crawledAt);
		job.setPosition(position);
		job.setLink(link);
		job.setTitle(title);
		job.setCompanyName(companyName);
		job.setCompanyLogoUrl(companyLogoUrl);
		job.setLocation(location);
		job.setWorkplaceType(workplaceType);
		job.setDetails(details);

		return job;
	}
}
