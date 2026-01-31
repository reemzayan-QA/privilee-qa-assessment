package pom;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PrivileeMapPage {

    private WebDriver driver;
    private WebDriverWait wait;

    /* ============================
       Constructors (IMPORTANT)
       ============================ */

    public PrivileeMapPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // Backward-compatible constructor for tests passing (driver, wait)
    public PrivileeMapPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = (wait != null)
                ? wait
                : new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /* ============================
       Locators
       ============================ */

    private final By mapContainer = By.cssSelector("[class*='map']");
    private final By filterButton = By.xpath("//button[contains(.,'Filter') or contains(.,'Filters')]");
    private final By filterPanel = By.cssSelector("[class*='filter']");
    private final By locationFilterButton = By.xpath("//button[contains(.,'Location')]");
    private final By clearFiltersButton = By.xpath("//button[contains(.,'Clear')]");
    private final By showVenuesButton = By.xpath("//button[contains(.,'Show')]");
    private final By venueCards = By.cssSelector("[class*='venue'], [class*='card']");
    private final By noResultsText = By.xpath("//*[contains(text(),'0 venue') or contains(text(),'No venues')]");
    private final By errorState = By.xpath("//*[contains(text(),'error') or contains(text(),'wrong')]");
    private final By loadingSpinner = By.cssSelector("[class*='loading'], [class*='spinner']");
    private final By markerLikeElements = By.cssSelector("[class*='marker'], svg circle");

    private final By venueTitleCandidates = By.cssSelector(
            "h1, h2, h3, [class*='title'], [class*='Title'], [class*='name'], [class*='Name']"
    );

    /* ============================
       Page load / smoke
       ============================ */

    public void open() {
        driver.get("https://staging-website.privilee.ae/map");
        wait.until(ExpectedConditions.visibilityOfElementLocated(mapContainer));
    }

    public boolean isLoaded() {
        return driver.findElements(mapContainer).size() > 0;
    }

    /* ============================
       Filter panel helpers
       ============================ */

    public void openFiltersPanel() {
        wait.until(ExpectedConditions.elementToBeClickable(filterButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(filterPanel));
    }

    public boolean isFiltersVisible() {
        return driver.findElements(filterPanel).size() > 0;
    }

    public void openLocationFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(locationFilterButton)).click();
    }

    public boolean isClearFiltersVisible() {
        return driver.findElements(clearFiltersButton).size() > 0;
    }

    public boolean isNoResultsVisible() {
        return driver.findElements(noResultsText).size() > 0;
    }

    public boolean isErrorVisible() {
        return driver.findElements(errorState).size() > 0;
    }

    public boolean isLoadingVisible() {
        return driver.findElements(loadingSpinner).size() > 0;
    }

    /* ============================
       Location selection
       ============================ */

    public boolean isLocationOptionPresent(String city) {
        return driver.findElements(
                By.xpath("//button[contains(normalize-space(.),'" + city + "')]")
        ).size() > 0;
    }

    public void selectLocation(String city) {
        By cityButton = By.xpath("//button[contains(normalize-space(.),'" + city + "')]");
        wait.until(ExpectedConditions.elementToBeClickable(cityButton)).click();
    }

    public String selectCityWithFallback(String requestedCity, String fallbackCity) {

        openFiltersPanel();
        openLocationFilter();

        if (isLocationOptionPresent(requestedCity)) {
            selectLocation(requestedCity);
            return requestedCity;
        }

        if (!isLocationOptionPresent(fallbackCity)) {
            throw new RuntimeException(
                    "Neither requested city nor fallback city available: "
                            + requestedCity + ", " + fallbackCity
            );
        }

        selectLocation(fallbackCity);
        return fallbackCity;
    }

    /* ============================
       Counts & state helpers
       ============================ */

    public int getVenueCardCount() {
        return driver.findElements(venueCards).size();
    }

    public int getMarkerLikeCount() {
        return driver.findElements(markerLikeElements).size();
    }

    private void waitShortForUpdate() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {
        }
    }

    /* ============================
       Normal filter flow
       ============================ */

    public boolean clickFirstFilterButtonAndDetectChange() {

        openFiltersPanel();

        int beforeCards = getVenueCardCount();
        int beforeMarkers = getMarkerLikeCount();

        By filterButtons = By.xpath(
                "//button[" +
                        "not(contains(.,'Clear')) and " +
                        "not(contains(.,'Show')) and " +
                        "string-length(normalize-space(.)) > 1" +
                        "]"
        );

        List<WebElement> buttons = driver.findElements(filterButtons);

        for (WebElement btn : buttons) {
            try {
                if (!btn.isDisplayed() || !btn.isEnabled()) continue;

                try {
                    btn.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                }

                waitShortForUpdate();

                boolean changed =
                        getVenueCardCount() != beforeCards ||
                        getMarkerLikeCount() != beforeMarkers;

                boolean hasState =
                        isClearFiltersVisible() ||
                        isNoResultsVisible() ||
                        isErrorVisible();

                if (changed || hasState) return true;

            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /* ============================
       Over-filtering edge case
       ============================ */

    public int applyFiltersToMinimizeVenues(String city, int maxClicks) {

        openFiltersPanel();
        openLocationFilter();
        selectLocation(city);

        By buttons = By.xpath(
                "//button[" +
                        "not(contains(.,'Clear')) and " +
                        "not(contains(.,'Show'))" +
                        "]"
        );

        List<WebElement> filters = driver.findElements(buttons);
        int clicks = 0;

        for (WebElement f : filters) {
            if (clicks >= maxClicks) break;

            try {
                if (f.isDisplayed() && f.isEnabled()) {
                    f.click();
                    waitShortForUpdate();
                    clicks++;
                }
            } catch (Exception ignored) {
            }
        }

        try {
            driver.findElement(showVenuesButton).click();
        } catch (Exception ignored) {
        }

        waitShortForUpdate();
        return getVenueCardCount();
    }

    /* ============================
       Data accuracy helper
       ============================ */

    public String getAnyVisibleVenueText() {

        List<WebElement> titles = driver.findElements(venueTitleCandidates);
        for (WebElement el : titles) {
            String text = safeText(el);
            if (text.length() >= 3) return text;
        }

        List<WebElement> cards = driver.findElements(venueCards);
        if (!cards.isEmpty()) {
            String text = safeText(cards.get(0));
            if (text.length() >= 3) return text;
        }

        return "";
    }

    private String safeText(WebElement el) {
        try {
            return el.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
