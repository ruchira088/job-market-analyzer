package com.ruchij.service.crawler.selenium.site;

import com.ruchij.service.crawler.selenium.site.pages.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class LinkedIn {
    private static final String WEB_URL = "https://www.linkedin.com";
    private final RemoteWebDriver remoteWebDriver;

    public LinkedIn(RemoteWebDriver remoteWebDriver) {
        this.remoteWebDriver = remoteWebDriver;
    }

    private void open() {
        this.remoteWebDriver.get(WEB_URL);
    }

    public HomePage login(String email, String password) {
        open();

        WebElement emailInput = remoteWebDriver.findElement(By.id("session_key"));
        emailInput.sendKeys(email);

        WebElement passwordInput = remoteWebDriver.findElement(By.id("session_password"));
        passwordInput.sendKeys(password);

        WebElement submitButton = remoteWebDriver.findElement(By.className("sign-in-form__submit-button"));
        submitButton.click();

        return new HomePage(remoteWebDriver);
    }

}
