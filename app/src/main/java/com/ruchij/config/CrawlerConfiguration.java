package com.ruchij.config;

import com.typesafe.config.Config;

public record CrawlerConfiguration(ElasticsearchConfiguration elasticsearchConfiguration,
                                   LinkedInCredentials linkedInCredentials) {

    public static CrawlerConfiguration parse(Config config) {
        return new CrawlerConfiguration(
            ElasticsearchConfiguration.parse(config.getConfig("elasticsearch")),
            LinkedInCredentials.parse(config.getConfig("linkedIn"))
        );
    }
}