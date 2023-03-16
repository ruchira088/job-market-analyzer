package com.ruchij.crawler.service.crawler.selenium.site.pages;

import com.ruchij.crawler.dao.job.models.WorkplaceType;
import com.ruchij.crawler.service.crawler.models.LinkedInJob;
import com.ruchij.crawler.service.crawler.selenium.driver.AwaitableWebDriver;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JobsPage {
	private static final Logger logger = LoggerFactory.getLogger(JobsPage.class);

	private final AwaitableWebDriver awaitableWebDriver;
	private boolean showingAllJobs = false;

	public JobsPage(AwaitableWebDriver awaitableWebDriver) {
		this.awaitableWebDriver = awaitableWebDriver;
	}

	static Optional<LinkedInJob> parse(String jobId, Instant timestamp, WebElement jobDetails, WebElement jobCard, String currentUrl) {
		try {
			URL pageUrl = new URL(currentUrl);

			String href = jobDetails.findElement(By.cssSelector(".jobs-unified-top-card__content--two-pane a.ember-view"))
				.getDomAttribute("href");

			URL jobUrl = new URL("%s://%s%s".formatted(pageUrl.getProtocol(), pageUrl.getHost(), href));

			Function<String, String> findText =
				cssSelector -> jobDetails.findElement(By.cssSelector(cssSelector)).getText();

			String title = findText.apply(".jobs-unified-top-card__job-title");
			String companyName = findText.apply(".jobs-unified-top-card__company-name");

			String companyLogoUrl =
				Optional.ofNullable(jobCard.findElement(By.cssSelector(".job-card-list__logo img")))
					.map(element -> element.getAttribute("src"))
					.orElse("https://via.placeholder.com/100x100");

			String location = findText.apply(".jobs-unified-top-card__bullet");

			String jobDescription = findText.apply(".jobs-description");

			List<WebElement> workplaceTypes =
				jobDetails.findElements(By.cssSelector(".jobs-unified-top-card__workplace-type"));

			Optional<WorkplaceType> workplaceType =
				workplaceTypes.isEmpty() ? Optional.empty() : WorkplaceType.parse(workplaceTypes.get(0).getText());

			LinkedInJob linkedInJob =
				new LinkedInJob(jobId, timestamp, jobUrl, title, companyName, companyLogoUrl, location, workplaceType, jobDescription);

			return Optional.of(linkedInJob);
		} catch (Exception exception) {
			logger.error("Error occurred parsing WebElement to a Job", exception);

			return Optional.empty();
		}
	}

	public int pageCount() {
		showAllJobs();

		String paginationAttribute = "data-test-pagination-page-btn";
		WebElement pagination = this.awaitableWebDriver.findElementByCss(".jobs-search-results-list__pagination");

		return pagination.findElements(By.cssSelector("li[%s]".formatted(paginationAttribute))).stream()
			.flatMap(element -> Optional.ofNullable(element.getAttribute(paginationAttribute)).stream())
			.flatMap(string -> {
				try {
					return Stream.of(Integer.parseInt(string));
				} catch (NumberFormatException numberFormatException) {
					return Stream.empty();
				}
			})
			.max(Comparator.naturalOrder())
			.orElse(1);
	}

	public Flowable<LinkedInJob> listJobs(Clock clock, String crawlerTaskId) {
		return Flowable.create(emitter ->
				traverseJobs(
					clock,
					crawlerTaskId,
					emitter::onNext,
					() -> !emitter.isCancelled(),
					emitter::onComplete
				),
			BackpressureStrategy.BUFFER
		);
	}

	void traverseJobs(Clock clock, String crawlerTaskId, Consumer<LinkedInJob> onLinkedInJob, Supplier<Boolean> shouldContinue, Runnable onComplete) {
		showAllJobs();
		Set<String> jobIds = new HashSet<>();
		int pageNumber = 1;

		boolean query = true;

		logger.info("Started crawling page=%s for id=%s".formatted(pageNumber, crawlerTaskId));

		while (query && shouldContinue.get()) {
			query = false;

			List<WebElement> jobCards = this.awaitableWebDriver.findElementsByCss(".job-card-list");

			for (WebElement jobCard : jobCards) {
				String jobId = jobCard.getAttribute("data-job-id");

				if (!shouldContinue.get()) {
					onComplete.run();
					return;
				} else if (!jobIds.contains(jobId)) {
					query = true;

					jobIds.add(jobId);
					jobCard.click();

					this.awaitableWebDriver.waitUntil((ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".artdeco-loader"))));

					WebElement jobDetails = this.awaitableWebDriver.findElementByCss(".jobs-details");
					parse(jobId, clock.instant(), jobDetails, jobCard, this.awaitableWebDriver.remoteWebDriver().getCurrentUrl()).ifPresent(onLinkedInJob);
				}
			}

			if (!query) {
				logger.info("Completed crawling page=%s for id=%s".formatted(pageNumber, crawlerTaskId));

				pageNumber++;

				List<WebElement> elements =
					this.awaitableWebDriver.findElementsByCss("li[data-test-pagination-page-btn='%s']".formatted(pageNumber));

				if (!elements.isEmpty()) {
					WebElement nextPage = elements.get(0);
					nextPage.click();
					query = true;

					logger.info("Started crawling page=%s for id=%s".formatted(pageNumber, crawlerTaskId));
				}
			}
		}

		onComplete.run();
	}

	void showAllJobs() {
		if (!showingAllJobs) {
			WebElement showAll = this.awaitableWebDriver.findElementByCss(".jobs-job-board-list__footer");
			showAll.click();

			this.awaitableWebDriver.findElementByCss(".jobs-details");
			showingAllJobs = true;
		}
	}

}
