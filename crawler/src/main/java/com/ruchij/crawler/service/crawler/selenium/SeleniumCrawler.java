package com.ruchij.crawler.service.crawler.selenium;

import com.ruchij.crawler.service.crawler.Crawler;
import com.ruchij.crawler.service.crawler.models.CrawledJob;
import com.ruchij.crawler.service.crawler.selenium.driver.AwaitableWebDriver;
import com.ruchij.crawler.service.crawler.selenium.driver.SeleniumWebDriver;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class SeleniumCrawler implements Crawler {
	private static final int JOBS_PER_PAGE = 24;

	private static final Logger logger = LoggerFactory.getLogger(SeleniumCrawler.class);

	private final Clock clock;

	public SeleniumCrawler(Clock clock) {
		this.clock = clock;
	}

	@Override
	public Flowable<CrawledJob> crawl(String crawlerTaskId, String email, String password) {
		logger.info("Started SeleniumCrawler id=%s".formatted(crawlerTaskId));

		return Flowable.fromSupplier(this::awaitableWebDriver)
			.concatMap(awaitableWebDriver -> {
				LinkedIn linkedIn = new LinkedIn(awaitableWebDriver);

				HomePage homePage = linkedIn.login(email, password);
				JobsPage jobsPage = homePage.jobsPage();

				int pageCount = jobsPage.pageCount();

				return jobsPage.listJobs(clock, crawlerTaskId)
					.subscribeOn(Schedulers.io())
					.zipWith(
						Flowable.range(1, Integer.MAX_VALUE),
						(linkedInJob, position) -> new CrawledJob(linkedInJob.job(crawlerTaskId, position), position * 100 / (pageCount * JOBS_PER_PAGE))
					)
					.doOnError(throwable -> logger.error("Error occurred with crawlerTaskId=%s".formatted(crawlerTaskId), throwable))
					.doOnCancel(() -> logger.info("SeleniumCrawler for crawlerTaskId=%s was cancelled".formatted(crawlerTaskId)))
					.doFinally(() -> {
						awaitableWebDriver.remoteWebDriver().quit();
						logger.info("Completed SeleniumCrawler for crawlerTaskId=%s".formatted(crawlerTaskId));
					});
			});
	}

	private AwaitableWebDriver awaitableWebDriver() {
		RemoteWebDriver remoteWebDriver = firefoxDriver();
		WebDriverWait webDriverWait = new WebDriverWait(remoteWebDriver, Duration.ofSeconds(15));

		return new SeleniumWebDriver(remoteWebDriver, webDriverWait);
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
			AwaitableWebDriver awaitableWebDriver = awaitableWebDriver();
			try {
				LinkedIn linkedIn = new LinkedIn(awaitableWebDriver);
				linkedIn.open();
				return true;
			} catch (Exception exception) {
				logger.error("Unable to open LinkedIn webpage", exception);
				return false;
			} finally {
				awaitableWebDriver.remoteWebDriver().quit();
			}
		});
	}
}
