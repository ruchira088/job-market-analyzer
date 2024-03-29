package com.ruchij.api;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.ruchij.api.config.ApiConfiguration;
import com.ruchij.api.dao.credentials.JdbiCredentialsDao;
import com.ruchij.api.dao.job.ElasticsearchSearchableJobDao;
import com.ruchij.api.dao.job.SearchableJobDao;
import com.ruchij.api.dao.user.JdbiUserDao;
import com.ruchij.api.kv.KeyValueStore;
import com.ruchij.api.kv.NamespacedKeyValueStore;
import com.ruchij.api.kv.RedisKeyValueStore;
import com.ruchij.api.pubsub.publisher.RedisPublisher;
import com.ruchij.api.pubsub.subscriber.RedisSubscriber;
import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.services.authentication.AuthenticationServiceImpl;
import com.ruchij.api.services.authentication.models.AuthenticationToken;
import com.ruchij.api.services.authorization.AuthorizationService;
import com.ruchij.api.services.authorization.AuthorizationServiceImpl;
import com.ruchij.api.services.crawler.ExtendedCrawlManagerImpl;
import com.ruchij.api.services.hashing.BCryptPasswordHashingService;
import com.ruchij.api.services.hashing.PasswordHashingService;
import com.ruchij.api.services.health.HealthService;
import com.ruchij.api.services.health.HealthServiceImpl;
import com.ruchij.api.services.lock.InMemoryLockService;
import com.ruchij.api.services.lock.LockService;
import com.ruchij.api.services.search.SearchService;
import com.ruchij.api.services.search.SearchServiceImpl;
import com.ruchij.api.services.user.UserService;
import com.ruchij.api.services.user.UserServiceImpl;
import com.ruchij.api.web.Routes;
import com.ruchij.api.web.plugins.ExceptionHandlerPlugin;
import com.ruchij.crawler.dao.jdbi.JdbiInitializer;
import com.ruchij.crawler.dao.linkedin.JdbiEncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.task.JdbiCrawlerTaskDao;
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
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import okhttp3.OkHttpClient;
import org.jdbi.v3.core.Jdbi;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

import static com.ruchij.crawler.utils.JsonUtils.objectMapper;

public class ApiApp {
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load();
		ApiConfiguration apiConfiguration = ApiConfiguration.parse(config);
		Routes routes = routes(apiConfiguration);

		httpApplication(routes, __ -> {})
			.start(apiConfiguration.httpConfiguration().host(), apiConfiguration.httpConfiguration().port());
	}

	public static Javalin httpApplication(
		Routes routes,
		Consumer<JavalinConfig> configModifier
	) {
		return Javalin
			.create(javalinConfig -> {
					javalinConfig.jsonMapper(new JavalinJackson(objectMapper, true));
					javalinConfig.registerPlugin(new ExceptionHandlerPlugin());
					javalinConfig.bundledPlugins.enableCors(corsPluginConfig ->
						corsPluginConfig.addRule(corsRule -> {
							corsRule.allowCredentials = true;
							corsRule.reflectClientOrigin = true;
						})
					);
					javalinConfig.router.apiBuilder(routes);
					configModifier.accept(javalinConfig);
				}
			);
	}

	public static Routes routes(ApiConfiguration apiConfiguration) throws Exception {
		ElasticsearchClientBuilder elasticsearchClientBuilder =
			new ElasticsearchClientBuilder(
				apiConfiguration.elasticsearchConfiguration(),
				new JacksonJsonpMapper(objectMapper)
			);
		ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();

		SearchableJobDao searchableJobDao =
			new ElasticsearchSearchableJobDao(elasticsearchAsyncClient, apiConfiguration.elasticsearchConfiguration().indexPrefix());

		DatabaseConfiguration databaseConfiguration = apiConfiguration.databaseConfiguration();

		Jdbi jdbi = Jdbi.create(databaseConfiguration.url(), databaseConfiguration.user(), databaseConfiguration.password());
		JdbiInitializer.initialize(jdbi);
		JdbiTransactor jdbiTransactor = new JdbiTransactor(jdbi);

		JdbiUserDao userDao = new JdbiUserDao();
		JdbiCredentialsDao credentialsDao = new JdbiCredentialsDao();
		JdbiCrawlerTaskDao crawlerTaskDao = new JdbiCrawlerTaskDao();
		JdbiEncryptedLinkedInCredentialsDao encryptedLinkedInCredentialsDao = new JdbiEncryptedLinkedInCredentialsDao();

		RedisKeyValueStore redisKeyValueStore =
			new RedisKeyValueStore(apiConfiguration.redisConfiguration().uri());

		KeyValueStore authenticationKeyValueStore =
			new NamespacedKeyValueStore(redisKeyValueStore, AuthenticationToken.class.getSimpleName());

		RandomGenerator<String> tokenGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);
		RandomGenerator<String> idGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);

		EncryptionService encryptionService =
			new AesEncryptionService(
				apiConfiguration.apiSecurityConfiguration().encryptionKey(),
				SecureRandom.getInstanceStrong()
			);

		Clock clock = Clock.systemUTC();

		Crawler crawler = new SeleniumCrawler(apiConfiguration.seleniumConfiguration(), idGenerator, clock);

		LinkedInCredentialsService linkedInCredentialsService =
			new LinkedInCredentialsServiceImpl<>(encryptedLinkedInCredentialsDao, jdbiTransactor, crawler, encryptionService, clock);

		SearchService searchService = new SearchServiceImpl<>(searchableJobDao, crawlerTaskDao, jdbiTransactor);

		CrawlManager crawlManager =
			new CrawlManagerImpl<>(
				crawler,
				crawlerTaskDao,
				jdbiTransactor,
				searchableJobDao,
				idGenerator,
				clock
			);

		ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
		LockService lockService = new InMemoryLockService(scheduledExecutorService, clock);

		RedisPublisher redisPublisher =
			new RedisPublisher(apiConfiguration.redisConfiguration().uri(), objectMapper);

		RedisSubscriber redisSubscriber =
			new RedisSubscriber(apiConfiguration.redisConfiguration().uri(), objectMapper);

		ExtendedCrawlManagerImpl extendedCrawlManager =
			new ExtendedCrawlManagerImpl(crawlManager, lockService, linkedInCredentialsService, redisPublisher, redisSubscriber);

		extendedCrawlManager.listen();

		PasswordHashingService passwordHashingService = new BCryptPasswordHashingService();

		UserService userService =
			new UserServiceImpl<>(
				userDao,
				credentialsDao,
				jdbiTransactor,
				passwordHashingService,
				idGenerator,
				clock
			);

		AuthenticationService authenticationService =
			new AuthenticationServiceImpl<>(
				authenticationKeyValueStore,
				tokenGenerator,
				passwordHashingService,
				userDao,
				credentialsDao,
				jdbiTransactor,
				clock
			);

		AuthorizationService authorizationService = new AuthorizationServiceImpl<>(crawlerTaskDao, jdbiTransactor, searchableJobDao);

		OkHttpClient httpClient =
			new OkHttpClient.Builder()
				.callTimeout(Duration.ofSeconds(20))
				.build();

		HealthService healthService =
			new HealthServiceImpl(
				elasticsearchAsyncClient,
				jdbi,
				redisKeyValueStore.getRedisAsyncCommands(),
				httpClient,
				crawler,
				scheduledExecutorService,
				System.getProperties(),
				clock
			);

		Routes routes =
			new Routes(
				extendedCrawlManager,
				userService,
				authenticationService,
				authorizationService,
				linkedInCredentialsService,
				searchService,
				healthService
			);

		return routes;
	}
}
