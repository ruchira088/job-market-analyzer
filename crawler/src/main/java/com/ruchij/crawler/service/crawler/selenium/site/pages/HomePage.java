package com.ruchij.crawler.service.crawler.selenium.site.pages;

import com.ruchij.crawler.service.crawler.selenium.driver.AwaitableWebDriver;
import org.openqa.selenium.WebElement;

public class HomePage {
	private final AwaitableWebDriver awaitableWebDriver;

	public HomePage(AwaitableWebDriver awaitableWebDriver) {
		this.awaitableWebDriver = awaitableWebDriver;
	}

	public JobsPage jobsPage() {
		this.awaitableWebDriver.remoteWebDriver().get("https://www.linkedin.com/jobs/collections/recommended/");
		this.awaitableWebDriver.findElementByCss(".jobs-details");

		return new JobsPage(this.awaitableWebDriver);
	}
}
