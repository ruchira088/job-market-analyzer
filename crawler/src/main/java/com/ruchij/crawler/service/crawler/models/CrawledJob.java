package com.ruchij.crawler.service.crawler.models;

import com.ruchij.crawler.dao.job.models.Job;

public record CrawledJob(Job job, int progress) {
}
