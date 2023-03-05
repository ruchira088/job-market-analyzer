package com.ruchij.crawler.service.crawler.selenium.site;

import com.ruchij.crawler.service.crawler.selenium.driver.AwaitableWebDriver;
import com.ruchij.crawler.service.crawler.selenium.site.pages.HomePage;
import org.openqa.selenium.WebElement;

public class LinkedIn {
	private static final String WEB_URL = "https://www.linkedin.com/login";
	private final AwaitableWebDriver awaitableWebDriver;

	public LinkedIn(AwaitableWebDriver awaitableWebDriver) {
		this.awaitableWebDriver = awaitableWebDriver;
	}

	public void open() {
		this.awaitableWebDriver.remoteWebDriver().get(WEB_URL);
		this.awaitableWebDriver.findElementByCss(".linkedin-logo");
	}

	public HomePage login(String email, String password) {
		open();

		WebElement emailInput = this.awaitableWebDriver.findElementByCss("#username");
		emailInput.sendKeys(email);

		WebElement passwordInput = this.awaitableWebDriver.findElementByCss("#password");
		passwordInput.sendKeys(password);

		WebElement submitButton = this.awaitableWebDriver.findElementByCss("button[aria-label='Sign in']");
		submitButton.click();

		return new HomePage(this.awaitableWebDriver);
	}

}
