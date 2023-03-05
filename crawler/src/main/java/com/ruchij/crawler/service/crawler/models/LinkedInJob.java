package com.ruchij.crawler.service.crawler.models;

import com.ruchij.crawler.dao.job.models.WorkplaceType;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public record LinkedInJob(String id, Instant crawledAt, URL link, String title, String companyName,
                          String location, Optional<WorkplaceType> workplaceType, String details) {
}
