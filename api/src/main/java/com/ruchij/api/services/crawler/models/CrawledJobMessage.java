package com.ruchij.api.services.crawler.models;

import com.ruchij.crawler.service.crawler.models.CrawledJob;

public record CrawledJobMessage(String userId, CrawledJob crawledJob) {
}
