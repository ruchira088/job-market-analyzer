package com.ruchij.site.pages;

import com.ruchij.dao.job.models.Job;
import com.ruchij.dao.job.models.WorkplaceType;
import com.ruchij.service.clock.Clock;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JobsPage {
    private final RemoteWebDriver remoteWebDriver;
    private final WebDriverWait webDriverWait;

    public JobsPage(RemoteWebDriver remoteWebDriver) {
        this.remoteWebDriver = remoteWebDriver;
        this.webDriverWait = new WebDriverWait(remoteWebDriver, Duration.ofSeconds(5));
    }

    public Flowable<Job> listJobs(Clock clock) {
        return Flowable.create(emitter ->
                traverseJobs(
                    clock,
                    emitter::onNext,
                    () -> !emitter.isCancelled(),
                    emitter::onComplete
                ),
            BackpressureStrategy.BUFFER
        );
    }

    void traverseJobs(Clock clock, Consumer<Job> onJob, Supplier<Boolean> shouldContinue, Runnable onComplete) {
        showAllJobs();
        Set<String> jobIds = new HashSet<>();
        int pageNumber = 1;

        boolean query = true;

        while (query && shouldContinue.get()) {
            query = false;

            List<WebElement> jobCards = remoteWebDriver.findElements(By.cssSelector(".job-card-list"));

            for (WebElement jobCard : jobCards) {
                String jobId = jobCard.getAttribute("data-job-id");

                if (!shouldContinue.get()) {
                    return;
                } else if (!jobIds.contains(jobId)) {
                    query = true;
                    jobIds.add(jobId);

                    jobCard.click();

                    webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".artdeco-loader")));

                    WebElement jobDetails = remoteWebDriver.findElement(By.cssSelector(".jobs-details"));
                    parse(jobId, clock.timestamp(), jobDetails, remoteWebDriver.getCurrentUrl()).ifPresent(onJob);
                }
            }

            if (!query) {
                pageNumber++;

                List<WebElement> elements =
                    remoteWebDriver.findElements(By.cssSelector("li[data-test-pagination-page-btn='%s']".formatted(pageNumber)));

                if (!elements.isEmpty()) {
                    WebElement nextPage = elements.get(0);
                    nextPage.click();
                    query = true;
                }
            }
        }

        onComplete.run();
    }

    static Optional<Job> parse(String jobId, Instant timestamp, WebElement jobDetails, String currentUrl) {
        try {
            URL pageUrl = new URL(currentUrl);

            String href = jobDetails.findElement(By.cssSelector(".jobs-unified-top-card__content--two-pane a.ember-view"))
                .getDomAttribute("href");

            URL jobUrl = new URL("%s://%s%s".formatted(pageUrl.getProtocol(), pageUrl.getHost(), href));

            Function<String, String> findText =
                cssSelector -> jobDetails.findElement(By.cssSelector(cssSelector)).getText();

            String title = findText.apply(".jobs-unified-top-card__job-title");
            String companyName = findText.apply(".jobs-unified-top-card__company-name");
            String location = findText.apply(".jobs-unified-top-card__bullet");

            String jobDescription = findText.apply(".jobs-description");

            List<WebElement> workplaceTypes =
                jobDetails.findElements(By.cssSelector(".jobs-unified-top-card__workplace-type"));

            Optional<WorkplaceType> workplaceType =
                workplaceTypes.isEmpty() ? Optional.empty() : WorkplaceType.parse(workplaceTypes.get(0).getText());

            Job job = new Job();
            job.setId(jobId);
            job.setCrawledAt(timestamp);
            job.setLink(jobUrl);
            job.setTitle(title);
            job.setCompanyName(companyName);
            job.setLocation(location);
            job.setWorkplaceType(workplaceType);
            job.setDetails(jobDescription);

            return Optional.of(job);
        } catch (Exception exception) {
            // TODO Add logging
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    void showAllJobs() {
        By showAllSelector = By.cssSelector(".jobs-job-board-list__footer");

        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(showAllSelector));
        WebElement showAll = remoteWebDriver.findElement(showAllSelector);

        showAll.click();

        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".jobs-details")));
    }

}
