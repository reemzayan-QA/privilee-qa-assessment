package pom;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class PrivileeMapPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public PrivileeMapPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    private By mapContainer = By.cssSelector("div[class*='map']");
    private By searchBox = By.cssSelector("input[type='search'], input[placeholder*='Search']");
    private By filterButton = By.xpath("//*[contains(text(),'Filter') or contains(text(),'Filters')]");

    public void open() {
        driver.get("https://staging-website.privilee.ae/map");
        wait.until(ExpectedConditions.visibilityOfElementLocated(mapContainer));
    }

    public boolean isLoaded() {
        return driver.findElements(mapContainer).size() > 0;
    }

    public boolean isSearchVisible() {
        return driver.findElements(searchBox).size() > 0;
    }

    public void openFilters() {
        if (driver.findElements(filterButton).size() > 0) {
            driver.findElement(filterButton).click();
        }
    }
}
