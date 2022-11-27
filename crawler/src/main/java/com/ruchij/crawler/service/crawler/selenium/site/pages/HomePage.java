package com.ruchij.crawler.service.crawler.selenium.site.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HomePage {
    private final RemoteWebDriver remoteWebDriver;

    public HomePage(RemoteWebDriver remoteWebDriver) {
        this.remoteWebDriver = remoteWebDriver;
    }

    public JobsPage jobsPage() {
        WebElement jobsTab = remoteWebDriver.findElement(By.cssSelector(".app-aware-link span[title=Jobs]"));
        jobsTab.click();

        return new JobsPage(remoteWebDriver);
    }
}
