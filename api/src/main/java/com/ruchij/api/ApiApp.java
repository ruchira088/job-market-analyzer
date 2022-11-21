package com.ruchij.api;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
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
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.security.SecureRandom;
import java.util.UUID;

import static com.ruchij.utils.JsonUtils.objectMapper;

public class ApiApp {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();
        ApiConfiguration apiConfiguration = ApiConfiguration.parse(config);

        Javalin.create(javalinConfig -> javalinConfig.jsonMapper(new JavalinJackson(objectMapper)))
            .routes(routes(apiConfiguration))
            .start(apiConfiguration.httpConfiguration().host(), apiConfiguration.httpConfiguration().port());
    }

    public static Routes routes(ApiConfiguration apiConfiguration) throws Exception {
        try (ElasticsearchClientBuilder elasticsearchClientBuilder = new ElasticsearchClientBuilder(apiConfiguration.elasticsearchConfiguration())) {
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
}
