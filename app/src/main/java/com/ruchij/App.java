package com.ruchij;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.ruchij.config.CrawlerConfiguration;
import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.job.models.Job;
import com.ruchij.dao.task.CrawlerTaskDao;
import com.ruchij.dao.task.models.CrawlerTask;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.crawler.selenium.SeleniumCrawler;
import com.ruchij.service.random.RandomGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Action;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class App {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();
        CrawlerConfiguration crawlerConfiguration = CrawlerConfiguration.parse(config);

        run(crawlerConfiguration);
    }

    public static void run(CrawlerConfiguration crawlerConfiguration) throws IOException {
        HttpHost elasticsearchHost =
            new HttpHost(
                crawlerConfiguration.elasticsearchConfiguration().host(),
                crawlerConfiguration.elasticsearchConfiguration().port()
            );

        try (RestClient restClient = RestClient.builder(elasticsearchHost).build();
             ElasticsearchTransport elasticsearchTransport = new RestClientTransport(restClient, new JacksonJsonpMapper())) {
            ElasticsearchAsyncClient elasticsearchAsyncClient = new ElasticsearchAsyncClient(elasticsearchTransport);


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
