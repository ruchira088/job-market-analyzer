package com.ruchij;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import com.ruchij.config.CrawlerConfiguration;
import com.ruchij.dao.elasticsearch.ElasticsearchClientBuilder;
import com.ruchij.dao.job.ElasticsearchJobDao;
import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.linkedin.ElasticsearchEncryptedLinkedInCredentialsDao;
import com.ruchij.dao.linkedin.EncryptedLinkedInCredentialsDao;
import com.ruchij.dao.task.CrawlerTaskDao;
import com.ruchij.dao.task.ElasticsearchCrawlerTaskDao;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.CrawlManager;
import com.ruchij.service.crawler.CrawlManagerImpl;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.crawler.selenium.SeleniumCrawler;
import com.ruchij.service.encryption.AesEncryptionService;
import com.ruchij.service.encryption.EncryptionService;
import com.ruchij.service.linkedin.LinkedInCredentialsService;
import com.ruchij.service.linkedin.LinkedInCredentialsServiceImpl;
import com.ruchij.service.random.RandomGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.security.SecureRandom;

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
            EncryptedLinkedInCredentialsDao encryptedLinkedInCredentialsDao = new ElasticsearchEncryptedLinkedInCredentialsDao(elasticsearchAsyncClient);

            Clock clock = Clock.systemClock();
            RandomGenerator<String> idGenerator = RandomGenerator.idGenerator();

            EncryptionService encryptionService =
                new AesEncryptionService(
                    crawlerConfiguration.crawlerSecurityConfiguration().encryptionKey(),
                    SecureRandom.getInstanceStrong()
                );

            LinkedInCredentialsService linkedInCredentialsService =
                new LinkedInCredentialsServiceImpl(encryptedLinkedInCredentialsDao, encryptionService, clock);

            Crawler crawler = new SeleniumCrawler(clock);

            CrawlManager crawlManager =
                new CrawlManagerImpl(
                    crawler,
                    linkedInCredentialsService,
                    crawlerTaskDao,
                    jobDao,
                    idGenerator,
                    clock
                );

            CrawlTaskRunner crawlTaskRunner = new CrawlTaskRunner(crawlManager, linkedInCredentialsService);

            crawlTaskRunner.run();
        }
    }
}
