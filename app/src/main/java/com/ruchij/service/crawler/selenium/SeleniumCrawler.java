package com.ruchij.service.crawler.selenium;

import com.ruchij.config.LinkedInCredentials;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.Crawler;
import com.ruchij.service.crawler.models.CrawledJob;
import com.ruchij.service.random.RandomGenerator;
import com.ruchij.service.crawler.selenium.site.LinkedIn;
import com.ruchij.service.crawler.selenium.site.pages.HomePage;
import com.ruchij.service.crawler.selenium.site.pages.JobsPage;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SeleniumCrawler implements Crawler {
    private final RemoteWebDriver remoteWebDriver;
    private final LinkedInCredentials linkedInCredentials;
    private final Clock clock;
    private final RandomGenerator<String> idGenerator;

    public SeleniumCrawler(
        RemoteWebDriver remoteWebDriver,
        LinkedInCredentials linkedInCredentials,
        Clock clock,
        RandomGenerator<String> idGenerator
    ) {
        this.remoteWebDriver = remoteWebDriver;
        this.linkedInCredentials = linkedInCredentials;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Override
    public Flowable<CrawledJob> crawl() {
        LinkedIn linkedIn = new LinkedIn(remoteWebDriver);

        HomePage homePage = linkedIn.login(linkedInCredentials.email(), linkedInCredentials.password());
        JobsPage jobsPage = homePage.jobsPage();

        int pageCount = jobsPage.pageCount();
        String crawlId = idGenerator.generate();

        return jobsPage.listJobs(clock, crawlId)
            .subscribeOn(Schedulers.io())
            .zipWith(
                Flowable.range(1, Integer.MAX_VALUE),
                (job, integer) -> new CrawledJob(crawlId, job, integer, pageCount)
            );
    }
}
