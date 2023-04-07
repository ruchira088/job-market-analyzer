package com.ruchij.crawler;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.ruchij.crawler.config.CrawlerConfiguration;
import com.ruchij.crawler.dao.job.ElasticsearchJobDao;
import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.linkedin.ElasticsearchEncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.linkedin.EncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;
import com.ruchij.crawler.dao.task.ElasticsearchCrawlerTaskDao;
import com.ruchij.crawler.dao.transaction.ElasticsearchTransactor;
import com.ruchij.crawler.dao.transaction.Transactor;
import com.ruchij.crawler.service.crawler.CrawlManager;
import com.ruchij.crawler.service.crawler.CrawlManagerImpl;
import com.ruchij.crawler.service.crawler.Crawler;
import com.ruchij.crawler.service.crawler.selenium.SeleniumCrawler;
import com.ruchij.crawler.service.encryption.AesEncryptionService;
import com.ruchij.crawler.service.encryption.EncryptionService;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsServiceImpl;
import com.ruchij.crawler.service.random.RandomGenerator;
import com.ruchij.crawler.utils.JsonUtils;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

public class CrawlerApp {
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load();
		CrawlerConfiguration crawlerConfiguration = CrawlerConfiguration.parse(config);

		run(crawlerConfiguration);
	}

	public static void run(CrawlerConfiguration crawlerConfiguration) throws Exception {
		try (ElasticsearchClientBuilder elasticsearchClientBuilder =
			     new ElasticsearchClientBuilder(
				     crawlerConfiguration.elasticsearchConfiguration(),
				     new JacksonJsonpMapper(JsonUtils.objectMapper)
			     )
		) {
			ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();
			Transactor<Void> transactor = new ElasticsearchTransactor();

			JobDao jobDao = new ElasticsearchJobDao(elasticsearchAsyncClient);
			CrawlerTaskDao crawlerTaskDao = new ElasticsearchCrawlerTaskDao(elasticsearchAsyncClient);
			EncryptedLinkedInCredentialsDao encryptedLinkedInCredentialsDao = new ElasticsearchEncryptedLinkedInCredentialsDao(elasticsearchAsyncClient);

			Clock clock = Clock.systemUTC();
			RandomGenerator<String> idGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);

			EncryptionService encryptionService =
				new AesEncryptionService(
					crawlerConfiguration.crawlerSecurityConfiguration().encryptionKey(),
					SecureRandom.getInstanceStrong()
				);

			LinkedInCredentialsService linkedInCredentialsService =
				new LinkedInCredentialsServiceImpl(encryptedLinkedInCredentialsDao, transactor, encryptionService, clock);

			Crawler crawler = new SeleniumCrawler(idGenerator, clock);

			CrawlManager crawlManager =
				new CrawlManagerImpl(
					crawler,
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
