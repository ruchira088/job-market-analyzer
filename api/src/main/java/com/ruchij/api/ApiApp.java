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
import com.ruchij.api.services.crawler.ExtendedCrawlManager;
import com.ruchij.api.services.crawler.ExtendedCrawlManagerImpl;
import com.ruchij.api.services.hashing.BCryptPasswordHashingService;
import com.ruchij.api.services.hashing.PasswordHashingService;
import com.ruchij.api.services.health.HealthService;
import com.ruchij.api.services.health.HealthServiceImpl;
import com.ruchij.api.services.lock.LocalLockService;
import com.ruchij.api.services.lock.LockService;
import com.ruchij.api.services.user.UserService;
import com.ruchij.api.services.user.UserServiceImpl;
import com.ruchij.api.web.Routes;
import com.ruchij.api.web.plugins.ExceptionHandlerPlugin;
import com.ruchij.crawler.dao.job.ElasticsearchJobDao;
import com.ruchij.crawler.dao.job.JobDao;
import com.ruchij.crawler.dao.linkedin.ElasticsearchEncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.linkedin.EncryptedLinkedInCredentialsDao;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;
import com.ruchij.crawler.dao.task.ElasticsearchCrawlerTaskDao;
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
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import okhttp3.OkHttpClient;

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
                    javalinConfig.jsonMapper(new JavalinJackson(objectMapper));
                    javalinConfig.plugins.register(new ExceptionHandlerPlugin());
                    javalinConfig.plugins.enableCors(
                        corsContainer -> corsContainer.add(corsPluginConfig -> {
                            corsPluginConfig.reflectClientOrigin = true;
                            corsPluginConfig.allowCredentials = true;
                        })
                    );
                    configModifier.accept(javalinConfig);
                }
            )
            .routes(routes);
    }

    public static Routes routes(ApiConfiguration apiConfiguration) throws Exception {
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

        LinkedInCredentialsService linkedInCredentialsService =
            new LinkedInCredentialsServiceImpl(encryptedLinkedInCredentialsDao, encryptionService, clock);

        Crawler crawler = new SeleniumCrawler(clock);

        CrawlManager crawlManager =
            new CrawlManagerImpl(
                crawler,
                crawlerTaskDao,
                jobDao,
                idGenerator,
                clock
            );

        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
        LockService lockService = new LocalLockService(scheduledExecutorService, clock);

        ExtendedCrawlManager extendedCrawlManager =
            new ExtendedCrawlManagerImpl(crawlManager, lockService, linkedInCredentialsService);

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

        OkHttpClient httpClient =
            new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(20))
                .build();

        HealthService healthService =
            new HealthServiceImpl(
                elasticsearchAsyncClient,
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
                linkedInCredentialsService,
                healthService
            );

        return routes;
    }
}
