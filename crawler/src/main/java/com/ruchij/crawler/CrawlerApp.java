package com.ruchij.crawler;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.ruchij.crawler.config.CrawlerConfiguration;
import com.ruchij.crawler.dao.jdbi.JdbiInitializer;
import com.ruchij.crawler.dao.job.ElasticsearchJobDao;
import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.linkedin.JdbiEncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.task.JdbiCrawlerTaskDao;
import com.ruchij.crawler.dao.transaction.ElasticsearchTransactor;
import com.ruchij.crawler.dao.transaction.JdbiTransactor;
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
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jdbi.v3.core.Jdbi;

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
			JobDao jobDao =
				new ElasticsearchJobDao(elasticsearchAsyncClient, crawlerConfiguration.elasticsearchConfiguration().indexPrefix());

			DatabaseConfiguration databaseConfiguration = crawlerConfiguration.databaseConfiguration();
			Jdbi jdbi = Jdbi.create(databaseConfiguration.url(), databaseConfiguration.user(), databaseConfiguration.password());
			JdbiInitializer.initialize(jdbi);
			JdbiTransactor jdbiTransactor = new JdbiTransactor(jdbi);

			JdbiCrawlerTaskDao crawlerTaskDao = new JdbiCrawlerTaskDao();
			JdbiEncryptedLinkedInCredentialsDao encryptedLinkedInCredentialsDao = new JdbiEncryptedLinkedInCredentialsDao();

			Clock clock = Clock.systemUTC();
			RandomGenerator<String> idGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);

			EncryptionService encryptionService =
				new AesEncryptionService(
					crawlerConfiguration.crawlerSecurityConfiguration().encryptionKey(),
					SecureRandom.getInstanceStrong()
				);

			Crawler crawler = new SeleniumCrawler(crawlerConfiguration.seleniumConfiguration(), idGenerator, clock);

			LinkedInCredentialsService linkedInCredentialsService =
				new LinkedInCredentialsServiceImpl<>(encryptedLinkedInCredentialsDao, jdbiTransactor, crawler, encryptionService, clock);

			CrawlManager crawlManager =
				new CrawlManagerImpl<>(
					crawler,
					crawlerTaskDao,
					jdbiTransactor,
					jobDao,
					idGenerator,
					clock
				);

			CrawlTaskRunner crawlTaskRunner = new CrawlTaskRunner(crawlManager, linkedInCredentialsService);

			crawlTaskRunner.run();
		}
	}
}
