package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Capture screenshot automatically on test failure.
     * Saved under: artifacts/selenium-reports/screenshots/
     */
    @AfterMethod(alwaysRun = true)
    public void teardown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE && driver != null) {

                Path dir = Path.of("artifacts", "selenium-reports", "screenshots");
                Files.createDirectories(dir);

                String testName =
                        result.getTestClass().getName() + "." + result.getMethod().getMethodName();
                testName = testName.replaceAll("[^a-zA-Z0-9._-]", "_");

                String timestamp =
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                Path target =
                        dir.resolve(testName + "_" + timestamp + ".png");

                File src =
                        ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

                Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("📸 Screenshot saved: " + target);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not capture screenshot: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
