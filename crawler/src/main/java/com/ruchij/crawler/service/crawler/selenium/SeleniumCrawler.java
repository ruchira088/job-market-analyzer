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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumCrawler implements Crawler {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumCrawler.class);

    private final Clock clock;

    public SeleniumCrawler(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Flowable<CrawledJob> crawl(String crawlerTaskId, String email, String password) {
        logger.info("Started SeleniumCrawler id=%s".formatted(crawlerTaskId));

        return Flowable.fromSupplier(ChromeDriver::new)
            .concatMap(chromeDriver -> {
                LinkedIn linkedIn = new LinkedIn(chromeDriver);

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
                        chromeDriver.quit();
                        logger.info("Completed SeleniumCrawler for crawlTaskId=%s".formatted(crawlerTaskId));
                    });
            });
    }
}
