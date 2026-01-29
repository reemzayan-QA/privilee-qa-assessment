package pom;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class PrivileeMapPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public PrivileeMapPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // Stable text anchors on https://staging-website.privilee.ae/map
    private final By filterYourSearchHeader =
            By.xpath("//*[contains(normalize-space(),'Filter your search')]"); // :contentReference[oaicite:1]{index=1}

    private final By filtersTitle =
            By.xpath("//*[normalize-space()='Filters' or contains(normalize-space(),'Filters')]"); // :contentReference[oaicite:2]{index=2}

    private final By loadingVenuesText =
            By.xpath("//*[contains(normalize-space(),'Loading venues')]"); // :contentReference[oaicite:3]{index=3}

    // Optional: "Location" section is visible in the filter panel
    private final By locationSection =
            By.xpath("//*[normalize-space()='Location']"); // :contentReference[oaicite:4]{index=4}

    public void open() {
        driver.get("https://staging-website.privilee.ae/map");

        // Wait until either the filter popup header appears OR the page shows "Loading venues..."
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(40));
        longWait.until(d -> d.findElements(filterYourSearchHeader).size() > 0
                || d.findElements(loadingVenuesText).size() > 0);
    }

    public boolean isLoaded() {
        // Consider page "loaded enough" if the filter header or loading venues text is present
        return driver.findElements(filterYourSearchHeader).size() > 0
                || driver.findElements(loadingVenuesText).size() > 0;
    }

    public boolean isFiltersVisible() {
        return driver.findElements(filtersTitle).size() > 0;
    }

    public boolean isLocationVisible() {
        return driver.findElements(locationSection).size() > 0;
    }
}
