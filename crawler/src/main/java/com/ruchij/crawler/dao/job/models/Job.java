package com.ruchij.crawler.dao.job.models;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public record Job(String id,
                  String linkedInId,
                  String crawlerTaskId,
                  Instant crawledAt,
                  int position,
                  URL link,
                  String title,
                  String companyName,
                  String companyLogoUrl,
                  String location,
                  Optional<WorkplaceType> workplaceType,
                  String details
) {
}