package com.ruchij.api;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.ruchij.api.config.ApiConfiguration;
import com.ruchij.api.dao.credentials.CredentialsDao;
import com.ruchij.api.dao.credentials.ElasticsearchCredentialsDao;
import com.ruchij.api.dao.user.ElasticsearchUserDao;
import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.kv.KeyValueStore;
import com.ruchij.api.kv.NamespacedKeyValueStore;
import com.ruchij.api.kv.RedisKeyValueStore;
import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.services.authentication.AuthenticationServiceImpl;
import com.ruchij.api.services.authentication.models.AuthenticationToken;
import com.ruchij.api.services.hashing.BCryptPasswordHashingService;
import com.ruchij.api.services.hashing.PasswordHashingService;
import com.ruchij.api.services.user.UserService;
import com.ruchij.api.services.user.UserServiceImpl;
import com.ruchij.api.web.Routes;
import com.ruchij.api.web.middleware.ExceptionHandler;
import com.ruchij.crawler.dao.job.ElasticsearchJobDao;
import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.linkedin.ElasticsearchEncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.linkedin.EncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;
import com.ruchij.crawler.dao.task.ElasticsearchCrawlerTaskDao;
import com.ruchij.crawler.service.clock.Clock;
import com.ruchij.crawler.service.crawler.CrawlManager;
import com.ruchij.crawler.service.crawler.CrawlManagerImpl;
import com.ruchij.crawler.service.crawler.Crawler;
import com.ruchij.crawler.service.crawler.selenium.SeleniumCrawler;
import com.ruchij.crawler.service.encryption.AesEncryptionService;
import com.ruchij.crawler.service.encryption.EncryptionService;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsServiceImpl;
import com.ruchij.crawler.service.random.RandomGenerator;
import com.ruchij.migration.elasticsearch.ElasticsearchClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.security.SecureRandom;
import java.util.UUID;

import static com.ruchij.crawler.utils.JsonUtils.objectMapper;

public class ApiApp {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();
        ApiConfiguration apiConfiguration = ApiConfiguration.parse(config);

        httpApplication(apiConfiguration);
    }

    private static Javalin httpApplication(ApiConfiguration apiConfiguration) throws Exception {
        Javalin httpApplication =
            Javalin.create(javalinConfig -> javalinConfig.jsonMapper(new JavalinJackson(objectMapper)))
                .routes(routes(apiConfiguration))
                .start(apiConfiguration.httpConfiguration().host(), apiConfiguration.httpConfiguration().port());

        return ExceptionHandler.handle(httpApplication);
    }

    private static Routes routes(ApiConfiguration apiConfiguration) throws Exception {
        ElasticsearchClientBuilder elasticsearchClientBuilder =
            new ElasticsearchClientBuilder(
                apiConfiguration.elasticsearchConfiguration(),
                new JacksonJsonpMapper(objectMapper)
            );
        ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();

        UserDao userDao = new ElasticsearchUserDao(elasticsearchAsyncClient);
        CredentialsDao credentialsDao = new ElasticsearchCredentialsDao(elasticsearchAsyncClient);
        JobDao jobDao = new ElasticsearchJobDao(elasticsearchAsyncClient);
        CrawlerTaskDao crawlerTaskDao = new ElasticsearchCrawlerTaskDao(elasticsearchAsyncClient);
        EncryptedLinkedInCredentialsDao encryptedLinkedInCredentialsDao = new ElasticsearchEncryptedLinkedInCredentialsDao(elasticsearchAsyncClient);

        KeyValueStore keyValueStore = new RedisKeyValueStore(apiConfiguration.redisConfiguration().uri());
        KeyValueStore authenticationKeyValueStore =
            new NamespacedKeyValueStore(keyValueStore, AuthenticationToken.class.getSimpleName());

        Clock clock = Clock.systemClock();
        RandomGenerator<String> tokenGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);
        RandomGenerator<String> idGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);

        EncryptionService encryptionService =
            new AesEncryptionService(
                apiConfiguration.apiSecurityConfiguration().encryptionKey(),
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

        PasswordHashingService passwordHashingService = new BCryptPasswordHashingService();

        UserService userService =
            new UserServiceImpl(
                userDao,
                credentialsDao,
                passwordHashingService,
                idGenerator,
                clock
            );

        AuthenticationService authenticationService =
            new AuthenticationServiceImpl(
                authenticationKeyValueStore,
                tokenGenerator,
                passwordHashingService,
                userDao,
                credentialsDao,
                clock
            );

        Routes routes = new Routes(crawlManager, userService, authenticationService, linkedInCredentialsService);

        return routes;
    }
}
