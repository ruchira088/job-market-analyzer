package com.ruchij.crawler.config;

import com.ruchij.migration.config.ElasticsearchConfiguration;
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