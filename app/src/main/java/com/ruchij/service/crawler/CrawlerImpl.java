package com.ruchij.service.crawler;

import com.ruchij.config.LinkedInCredentials;
import com.ruchij.dao.job.JobDao;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.crawler.models.CrawlProgress;
import com.ruchij.service.random.RandomGenerator;
import com.ruchij.site.LinkedIn;
import com.ruchij.site.pages.HomePage;
import com.ruchij.site.pages.JobsPage;
import io.reactivex.rxjava3.core.Flowable;
import org.openqa.selenium.remote.RemoteWebDriver;

public class CrawlerImpl implements Crawler {
    private final RemoteWebDriver remoteWebDriver;
    private final JobDao jobDao;
    private final LinkedInCredentials linkedInCredentials;
    private final Clock clock;
    private final RandomGenerator<String> idGenerator;

    public CrawlerImpl(
        RemoteWebDriver remoteWebDriver,
        JobDao jobDao,
        LinkedInCredentials linkedInCredentials,
        Clock clock,
        RandomGenerator<String> idGenerator
    ) {
        this.remoteWebDriver = remoteWebDriver;
        this.jobDao = jobDao;
        this.linkedInCredentials = linkedInCredentials;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Override
    public Flowable<CrawlProgress> crawl() {
        LinkedIn linkedIn = new LinkedIn(remoteWebDriver);

        HomePage homePage = linkedIn.login(linkedInCredentials.email(), linkedInCredentials.password());
        JobsPage jobsPage = homePage.jobsPage();

        int pageCount = jobsPage.pageCount();
        String crawlId = idGenerator.generate();

        return jobsPage.listJobs(clock, crawlId)
            .flatMap(job -> Flowable.fromCompletionStage(jobDao.insert(job)))
            .zipWith(
                Flowable.range(1, Integer.MAX_VALUE),
                (id, integer) -> new CrawlProgress(crawlId, clock.timestamp(), integer, pageCount)
            );
    }
}
