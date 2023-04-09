package com.ruchij.crawler.service.crawler.selenium.site;

import com.ruchij.crawler.service.crawler.selenium.driver.AwaitableWebDriver;
import com.ruchij.crawler.service.crawler.selenium.site.pages.HomePage;
import org.openqa.selenium.WebElement;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

		CompletableFuture<Boolean> isSuccess =
			CompletableFuture.supplyAsync(() -> {
				this.awaitableWebDriver.findElementByCss(".global-nav__me");
				return true;
			});

		CompletableFuture<Boolean> errorMessageNotVisible = CompletableFuture.supplyAsync(() -> {
			boolean isValid = true;

			while (isValid && !isSuccess.isDone()) {
				WebElement passwordError = this.awaitableWebDriver.findElementByCss("#error-for-password");
				isValid = passwordError.getText().isEmpty();

				if (isValid) {
					try {
						Thread.sleep(250);
					} catch (InterruptedException ignored) {
					}
				}
			}

			return isValid;
		});

		Boolean success;

		try {
			success = (Boolean) CompletableFuture.anyOf(isSuccess, errorMessageNotVisible).get(10, TimeUnit.SECONDS);
		} catch (Exception exception) {
			throw new RuntimeException("Something went wrong");
		}

		if (!success) {
			throw new RuntimeException("LinkedIn login error visible");
		}

		return new HomePage(this.awaitableWebDriver);
	}

}
