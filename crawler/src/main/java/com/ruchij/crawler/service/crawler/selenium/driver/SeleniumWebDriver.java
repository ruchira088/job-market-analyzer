package com.ruchij.crawler.service.crawler.selenium.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.function.Function;

public class SeleniumWebDriver implements AwaitableWebDriver {
	private final RemoteWebDriver remoteWebDriver;
	private final WebDriverWait webDriverWait;

	public SeleniumWebDriver(RemoteWebDriver remoteWebDriver, WebDriverWait webDriverWait) {
		this.remoteWebDriver = remoteWebDriver;
		this.webDriverWait = webDriverWait;
	}

	@Override
	public WebElement findElementByCss(String cssQuery) {
		By cssSelector = By.cssSelector(cssQuery);
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(cssSelector));

		return remoteWebDriver.findElement(cssSelector);
	}

	@Override
	public List<WebElement> findElementsByCss(String cssQuery) {
		By cssSelector = By.cssSelector(cssQuery);
		webDriverWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(cssSelector));

		return remoteWebDriver.findElements(cssSelector);
	}

	@Override
	public <V> V waitUntil(Function<? super WebDriver, V> isTrue) {
		return this.webDriverWait.until(isTrue);
	}

	@Override
	public RemoteWebDriver remoteWebDriver() {
		return this.remoteWebDriver;
	}
}
