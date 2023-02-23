package com.ruchij.crawler.service.crawler.selenium.site;

import com.ruchij.crawler.service.crawler.selenium.site.pages.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LinkedIn {
    private static final String WEB_URL = "https://www.linkedin.com/login";
    private final RemoteWebDriver remoteWebDriver;
    private final WebDriverWait webDriverWait;

    public LinkedIn(RemoteWebDriver remoteWebDriver) {
        this.remoteWebDriver = remoteWebDriver;
        this.webDriverWait = new WebDriverWait(remoteWebDriver, Duration.ofSeconds(5));
    }

    public void open() {
        this.remoteWebDriver.get(WEB_URL);

        this.webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("linkedin-logo")));
    }

    public HomePage login(String email, String password) {
        open();

        WebElement emailInput = remoteWebDriver.findElement(By.id("username"));
        emailInput.sendKeys(email);

        WebElement passwordInput = remoteWebDriver.findElement(By.id("password"));
        passwordInput.sendKeys(password);

        WebElement submitButton = remoteWebDriver.findElement(By.cssSelector("button[aria-label='Sign in']"));
        submitButton.click();

        return new HomePage(remoteWebDriver, webDriverWait);
    }

}
