package com.ruchij;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import com.ruchij.config.CrawlerConfiguration;
import com.ruchij.dao.elasticsearch.ElasticsearchClientBuilder;
import com.ruchij.dao.job.JobDao;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.crawler.selenium.SeleniumCrawler;
import com.ruchij.service.random.RandomGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjava3.core.Flowable;

import java.util.concurrent.CompletableFuture;

public class App {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();
        CrawlerConfiguration crawlerConfiguration = CrawlerConfiguration.parse(config);

        run(crawlerConfiguration);
    }

    public static void run(CrawlerConfiguration crawlerConfiguration) throws Exception {
        try (ElasticsearchClientBuilder elasticsearchClientBuilder = new ElasticsearchClientBuilder(crawlerConfiguration.elasticsearchConfiguration())) {
            ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();

//            JobDao jobDao = new ElasticsearchJobDao(elasticsearchAsyncClient);
            Clock clock = Clock.systemClock();
            RandomGenerator<String> idGenerator = RandomGenerator.idGenerator();

            JobDao jobDao = job -> {
                System.out.println(job);
                return CompletableFuture.completedFuture(idGenerator.generate());
            };

            Crawler crawler =
                new SeleniumCrawler(crawlerConfiguration.linkedInCredentials(), clock, idGenerator);

            run();
        }
    }

    public static void run(
    ) {
       Flowable.range(1, 10)
           .doOnComplete(() -> {
               System.out.println("Complete");
           })
           .take(4)
           .subscribe(System.out::println);
    }
}
