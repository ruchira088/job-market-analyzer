package com.ruchij;

import com.ruchij.service.clock.SystemClock;
import com.ruchij.site.LinkedIn;
import com.ruchij.site.pages.HomePage;
import com.ruchij.site.pages.JobsPage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.openqa.selenium.chrome.ChromeDriver;

public class App {
    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        System.out.println(config);

//        ChromeDriver chromeDriver = new ChromeDriver();
//
//        LinkedIn linkedIn = new LinkedIn(chromeDriver);
//        HomePage homePage = linkedIn.login("ruchira088@gmail.com", "");
//        JobsPage jobsPage = homePage.jobsPage();
//        jobsPage.listJobs(new SystemClock()).take(4)
//            .subscribe(job -> {
//                System.out.println(job);
//            });
//
//        chromeDriver.close();
//        chromeDriver.quit();


//        chromeDriver.get("https://video.home.ruchij.com/");
//
////        driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("deferred-class-name")));
//
//        File screenshot = chromeDriver.getScreenshotAs(OutputType.FILE);
//        Path result = Paths.get("/Users/ruchira/Development/job-market-analyzer/screenshot.png");
//
//        Files.copy(screenshot.toPath(), result, StandardCopyOption.REPLACE_EXISTING);
//
//        chromeDriver.close();
    }
}
