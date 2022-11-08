package com.ruchij.config;

import com.typesafe.config.Config;

public record CrawlerConfiguration(ElasticsearchConfiguration elasticsearchConfiguration,
                                   SecurityConfiguration securityConfiguration) {

    public static CrawlerConfiguration parse(Config config) {
        return new CrawlerConfiguration(
            ElasticsearchConfiguration.parse(config.getConfig("elasticsearch")),
            SecurityConfiguration.parse(config.getConfig("security"))
        );
    }
}