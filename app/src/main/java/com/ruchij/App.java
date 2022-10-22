package com.ruchij;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.ruchij.config.CrawlerConfiguration;
import com.ruchij.config.LinkedInCredentials;
import com.ruchij.dao.job.ElasticsearchJobDao;
import com.ruchij.dao.job.JobDao;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.crawler.CrawlerImpl;
import com.ruchij.service.random.RandomGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load();
        CrawlerConfiguration crawlerConfiguration = CrawlerConfiguration.parse(config);


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

            JobDao jobDao = new ElasticsearchJobDao(elasticsearchAsyncClient);
        }
    }

    public static void run(
        RemoteWebDriver remoteWebDriver,
        JobDao jobDao,
        LinkedInCredentials linkedInCredentials,
        Clock clock,
        RandomGenerator<String> idGenerator
    ) {
        Crawler crawler = new CrawlerImpl(remoteWebDriver, jobDao, linkedInCredentials, clock, idGenerator);

    }
}
