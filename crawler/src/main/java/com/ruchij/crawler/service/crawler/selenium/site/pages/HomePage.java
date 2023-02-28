package com.ruchij.crawler.service.crawler.selenium.site.pages;

import com.ruchij.crawler.service.crawler.selenium.driver.AwaitableWebDriver;
import org.openqa.selenium.WebElement;

public class HomePage {
    private final AwaitableWebDriver awaitableWebDriver;

    public HomePage(AwaitableWebDriver awaitableWebDriver) {
        this.awaitableWebDriver = awaitableWebDriver;
    }

    public JobsPage jobsPage() {
        WebElement jobsTab = this.awaitableWebDriver.findElementByCss(".app-aware-link span[title=Jobs]");
        jobsTab.click();

        return new JobsPage(this.awaitableWebDriver);
    }
}
