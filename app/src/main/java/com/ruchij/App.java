package com.ruchij;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import com.ruchij.config.CrawlerConfiguration;
import com.ruchij.dao.elasticsearch.ElasticsearchClientBuilder;
import com.ruchij.dao.job.ElasticsearchJobDao;
import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.linkedin.ElasticsearchLinkedInCredentialsDao;
import com.ruchij.dao.linkedin.LinkedInCredentialsDao;
import com.ruchij.dao.task.CrawlerTaskDao;
import com.ruchij.dao.task.ElasticsearchCrawlerTaskDao;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.crawler.selenium.SeleniumCrawler;
import com.ruchij.service.encryption.AesEncryptionService;
import com.ruchij.service.encryption.EncryptionService;
import com.ruchij.service.random.RandomGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class App {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();
        CrawlerConfiguration crawlerConfiguration = CrawlerConfiguration.parse(config);

        run(crawlerConfiguration);
    }

    public static void run(CrawlerConfiguration crawlerConfiguration) throws Exception {
        try (ElasticsearchClientBuilder elasticsearchClientBuilder = new ElasticsearchClientBuilder(crawlerConfiguration.elasticsearchConfiguration())) {
            ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();

            JobDao jobDao = new ElasticsearchJobDao(elasticsearchAsyncClient);
            CrawlerTaskDao crawlerTaskDao = new ElasticsearchCrawlerTaskDao(elasticsearchAsyncClient);
            LinkedInCredentialsDao linkedInCredentialsDao = new ElasticsearchLinkedInCredentialsDao(elasticsearchAsyncClient);

            Clock clock = Clock.systemClock();
            RandomGenerator<String> idGenerator = RandomGenerator.idGenerator();

            EncryptionService encryptionService =
                new AesEncryptionService(crawlerConfiguration.securityConfiguration().encryptionKey());

            Crawler crawler = new SeleniumCrawler(clock);

            CrawlTaskRunner crawlTaskRunner =
                new CrawlTaskRunner(
                    crawler,
                    crawlerTaskDao,
                    jobDao,
                    linkedInCredentialsDao,
                    encryptionService,
                    clock,
                    idGenerator
                );

            crawlTaskRunner.run();
        }
    }
}
