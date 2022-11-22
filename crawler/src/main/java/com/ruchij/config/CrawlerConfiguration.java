package com.ruchij.config;

import com.typesafe.config.Config;

public record CrawlerConfiguration(ElasticsearchConfiguration elasticsearchConfiguration,
                                   CrawlerSecurityConfiguration crawlerSecurityConfiguration) {

    public static CrawlerConfiguration parse(Config config) {
        return new CrawlerConfiguration(
            ElasticsearchConfiguration.parse(config.getConfig("elasticsearch")),
            CrawlerSecurityConfiguration.parse(config.getConfig("security"))
        );
    }
}