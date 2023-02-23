package com.ruchij.crawler.service.crawler.selenium.site.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage {
    private final RemoteWebDriver remoteWebDriver;
    private final WebDriverWait webDriverWait;

    public HomePage(RemoteWebDriver remoteWebDriver, WebDriverWait webDriverWait) {
        this.remoteWebDriver = remoteWebDriver;
        this.webDriverWait = webDriverWait;
    }

    public JobsPage jobsPage() {
        By jobsTabSelector = By.cssSelector(".app-aware-link span[title=Jobs]");

        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(jobsTabSelector));

        WebElement jobsTab = remoteWebDriver.findElement(jobsTabSelector);
        jobsTab.click();

        return new JobsPage(remoteWebDriver, webDriverWait);
    }
}
