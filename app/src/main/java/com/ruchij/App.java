package com.ruchij;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.ruchij.config.CrawlerConfiguration;
import com.ruchij.dao.job.JobDao;
import com.ruchij.dao.job.models.Job;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.crawler.selenium.SeleniumCrawler;
import com.ruchij.service.random.RandomGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class App {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();
        CrawlerConfiguration crawlerConfiguration = CrawlerConfiguration.parse(config);

//        CompletableFuture completableFuture = new CompletableFuture();
//
//        Flowable.range(1, 10).map(i -> {
//                System.out.println("Processing " + i);
//                Thread.sleep(1_000);
//                System.out.println(Thread.currentThread().getName());
//                System.out.println("Processed: " + i);
//                return 1;
//            })
//            .subscribeOn(Schedulers.io(), true)
////            .observeOn(Schedulers.io())
//            .zipWith(Flowable.range(100, 10), Integer::sum)
////            .subscribeOn(Schedulers.io())
////            .observeOn(Schedulers.io())
//            .doOnComplete(() -> {
//                completableFuture.complete(true);
//            })
//            .subscribe(result -> {
//                System.out.println("** " + Thread.currentThread().getName() + " " + result);
//            });
//
//        completableFuture.get();

        run(crawlerConfiguration);


//        ChromeDriver chromeDriver = new ChromeDriver();
//
//        LinkedIn linkedIn = new LinkedIn(chromeDriver);
//        HomePage homePage = linkedIn.login("ruchira088@gmail.com", "");
//        JobsPage jobsPage = homePage.jobsPage();
//        jobsPage.listJobs(new SystemClock()).take(4)
//            .subscribe(job -> {
//                System.out.println(job);
//            });
//
//        chromeDriver.close();
//        chromeDriver.quit();


//        chromeDriver.get("https://video.home.ruchij.com/");
//
////        driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("deferred-class-name")));
//
//        File screenshot = chromeDriver.getScreenshotAs(OutputType.FILE);
//        Path result = Paths.get("/Users/ruchira/Development/job-market-analyzer/screenshot.png");
//
//        Files.copy(screenshot.toPath(), result, StandardCopyOption.REPLACE_EXISTING);
//
//        chromeDriver.close();
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


            ChromeDriver chromeDriver = new ChromeDriver();
//            JobDao jobDao = new ElasticsearchJobDao(elasticsearchAsyncClient);
            Clock clock = Clock.systemClock();
            RandomGenerator<String> idGenerator = RandomGenerator.idGenerator();

            JobDao jobDao = new JobDao() {
                @Override
                public CompletableFuture<String> insert(Job job) {
                    System.out.println(job);
                    return CompletableFuture.completedFuture(idGenerator.generate());
                }
            };

            Crawler crawler =
                new SeleniumCrawler(chromeDriver, crawlerConfiguration.linkedInCredentials(), clock, idGenerator);

            run(crawler, jobDao);
        }
    }

    public static void run(
        Crawler crawler,
        JobDao jobDao
    ) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        crawler.crawl()
            .take(4)
            .doOnComplete(() -> completableFuture.complete(null))
            .subscribe(System.out::println);

        try {
            completableFuture.get();
        } catch (Exception exception) {

        }
    }
}
