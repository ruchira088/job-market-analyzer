package com.ruchij.crawler.service.crawler.selenium;

import com.ruchij.crawler.service.clock.Clock;
import com.ruchij.crawler.service.crawler.Crawler;
import com.ruchij.crawler.service.crawler.models.CrawledJob;
import com.ruchij.crawler.service.crawler.selenium.site.LinkedIn;
import com.ruchij.crawler.service.crawler.selenium.site.pages.HomePage;
import com.ruchij.crawler.service.crawler.selenium.site.pages.JobsPage;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class SeleniumCrawler implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumCrawler.class);

    private final Clock clock;

    public SeleniumCrawler(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Flowable<CrawledJob> crawl(String crawlerTaskId, String email, String password) {
        logger.info("Started SeleniumCrawler id=%s".formatted(crawlerTaskId));

        return Flowable.fromSupplier(this::remoteWebDriver)
            .concatMap(remoteWebDriver -> {
                LinkedIn linkedIn = new LinkedIn(remoteWebDriver);

                HomePage homePage = linkedIn.login(email, password);
                JobsPage jobsPage = homePage.jobsPage();

                int pageCount = jobsPage.pageCount();

                return jobsPage.listJobs(clock, crawlerTaskId)
                    .subscribeOn(Schedulers.io())
                    .zipWith(
                        Flowable.range(1, Integer.MAX_VALUE),
                        (job, integer) -> new CrawledJob(crawlerTaskId, job, integer, pageCount)
                    )
                    .doOnError(throwable -> logger.error("Error occurred with crawlTaskId=%s".formatted(crawlerTaskId), throwable))
                    .doFinally(() -> {
                        remoteWebDriver.quit();
                        logger.info("Completed SeleniumCrawler for crawlTaskId=%s".formatted(crawlerTaskId));
                    });
            });
    }

    private RemoteWebDriver remoteWebDriver() {
        return firefoxDriver();
    }

    private FirefoxDriver firefoxDriver() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setHeadless(true);

        FirefoxDriver firefoxDriver = new FirefoxDriver(firefoxOptions);

        return firefoxDriver;
    }

    private ChromeDriver chromeDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
        chromeOptions.addArguments(
            "--disable-dev-shm-usage",
            "--no-sandbox"
        );

        ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);

        return chromeDriver;
    }

    @Override
    public CompletableFuture<Boolean> isHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            RemoteWebDriver remoteWebDriver = remoteWebDriver();

            try {
                LinkedIn linkedIn = new LinkedIn(remoteWebDriver);
                linkedIn.open();
                return true;
            } catch (Exception exception) {
                logger.error("Unable to open LinkedIn webpage", exception);
                return false;
            } finally {
                remoteWebDriver.quit();
            }
        });
    }
}
