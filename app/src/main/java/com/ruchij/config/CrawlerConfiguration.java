package com.ruchij.config;

public record CrawlerConfiguration(ElasticsearchConfiguration elasticsearchConfiguration,
                                   LinkedInCredentials linkedInCredentials) {
}