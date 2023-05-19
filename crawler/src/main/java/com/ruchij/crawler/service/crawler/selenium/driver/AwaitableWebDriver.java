package com.ruchij.crawler.service.crawler.selenium.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.Closeable;
import java.util.List;
import java.util.function.Function;

public interface AwaitableWebDriver extends Closeable {
	WebElement findElementByCss(String cssQuery);

	List<WebElement> findElementsByCss(String cssQuery);

	RemoteWebDriver remoteWebDriver();

	<V> V waitUntil(Function<? super WebDriver, V> isTrue);
}
