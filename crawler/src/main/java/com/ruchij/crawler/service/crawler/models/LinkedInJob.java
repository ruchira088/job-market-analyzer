package com.ruchij.crawler.service.crawler.models;

import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.dao.job.models.WorkplaceType;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public record LinkedInJob(String id, Instant crawledAt, URL link, String title, String companyName,
                          String companyLogoUrl,
                          String location, Optional<WorkplaceType> workplaceType, String details) {
	public Job job(String jobId, String crawlerTaskId, int position) {
		return new Job(
			jobId,
			id,
			crawlerTaskId,
			crawledAt,
			position,
			link,
			title,
			companyName,
			companyLogoUrl,
			location,
			workplaceType,
			details
		);
	}
}
