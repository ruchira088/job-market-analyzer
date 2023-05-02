package com.ruchij.crawler.config;

import com.ruchij.crawler.service.crawler.selenium.Browser;
import com.typesafe.config.Config;

public record SeleniumConfiguration(Browser browser, boolean headlessMode) {
	public static SeleniumConfiguration parse(Config config) {
		Browser browser = config.getEnum(Browser.class, "browser");
		boolean isHeadlessMode = config.getBoolean("headless-mode");

		return new SeleniumConfiguration(browser, isHeadlessMode);
	}
}
