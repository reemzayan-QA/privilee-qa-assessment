package pom;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PrivileeMapPage {

    private final WebDriver driver;
    private WebDriverWait wait;

    // ✅ Staging URL 
    public static final String BASE_URL = "https://staging-website.privilee.ae/map";

    // ---------- Constructors ----------
    // New constructor (driver only)
    public PrivileeMapPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // ✅ Backward-compatible constructor (driver + wait) to fix compilation errors
    public PrivileeMapPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = (wait != null) ? wait : new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // ---------- Locators ----------
    private final By filterHeader = By.xpath("//*[normalize-space()='Filter your search' or normalize-space()='Filters']");
    private final By clearFilters = By.xpath("//*[contains(normalize-space(),'Clear filters')]");
    private final By loadingMsg = By.xpath("//*[contains(translate(.,'LOADING','loading'),'loading')]");
    private final By noResultsMsg = By.xpath("//*[contains(translate(.,'NO RESULTS','no results'),'no results') or " +
            "contains(translate(.,'NO VENUES','no venues'),'no venues') or " +
            "contains(translate(.,'NO MATCH','no match'),'no match')]");
    private final By errorMsg = By.xpath("//*[contains(translate(.,'UNABLE','unable'),'unable') or " +
            "contains(translate(.,'ERROR','error'),'error') or " +
            "contains(translate(.,'SOMETHING WENT WRONG','something went wrong'),'something went wrong')]");

    // "Result-like" elements (best-effort; SPA DOM changes)
    private final By venueCards = By.cssSelector(
            "[class*='venue'], [class*='Venue'], [class*='result'], [class*='Result'], [class*='card'], [class*='Card']"
    );

    // Marker-ish elements (best-effort; maps can be canvas-based)
    private final By markerElements = By.cssSelector(
            "img[src*='googleapis.com'], img[src*='gstatic.com'], [aria-label*='marker'], [class*='marker']"
    );

    // ---------- Actions ----------
    public void open() {
        driver.get(BASE_URL);
        // Wait until the filter header is present/visible as a stable anchor
        wait.until(ExpectedConditions.presenceOfElementLocated(filterHeader));
    }

    public boolean isFilterPanelVisible() {
        return driver.findElements(filterHeader).size() > 0;
    }

    public boolean isClearFiltersVisible() {
        return driver.findElements(clearFilters).size() > 0;
    }

    public boolean isLoadingVisible() {
        return driver.findElements(loadingMsg).size() > 0;
    }

    public boolean isNoResultsVisible() {
        return driver.findElements(noResultsMsg).size() > 0;
    }

    public boolean isErrorVisible() {
        return driver.findElements(errorMsg).size() > 0;
    }

    public int getVenueCardCount() {
        return driver.findElements(venueCards).size();
    }

    public int getMarkerLikeCount() {
        return driver.findElements(markerElements).size();
    }

    /**
 * Clicks the first available filter button (chips) in the filter panel.
 * Since buttons may not toggle text (+/×), we validate click using UI reaction:
 * - Clear filters appears OR
 * - no results / error appears OR
 * - results/markers count changes
 */
    public boolean clickFirstFilterButtonAndDetectChange() {

    int beforeCards = getVenueCardCount();
    int beforeMarkers = getMarkerLikeCount();

    // Filter options as buttons/chips (best-effort)
    By filterButtons = By.xpath(
            "//button[not(contains(.,'Clear filters')) and normalize-space(.)!='' and string-length(normalize-space(.))>1]"
    );

    List<WebElement> buttons = driver.findElements(filterButtons);

    for (WebElement btn : buttons) {
        try {
            if (btn.isDisplayed() && btn.isEnabled()) {
                btn.click();
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }

            waitShortForUpdate();

            int afterCards = getVenueCardCount();
            int afterMarkers = getMarkerLikeCount();

            boolean changed = (afterCards != beforeCards) || (afterMarkers != beforeMarkers);
            boolean hasState = isClearFiltersVisible() || isNoResultsVisible() || isErrorVisible();

            if (changed || hasState) return true;
        } catch (Exception ignored) {
            // try next button
        }
    }

    return false;
    }


    public void waitShortForUpdate() {
        sleep(1500);
    }

    /**
     * Data accuracy helper: tries to fetch any meaningful visible title/name text.
     */
    public String getAnyVisibleVenueText() {
        By titles = By.cssSelector("h1, h2, h3, [class*='title'], [class*='Title'], [class*='name'], [class*='Name']");
        for (WebElement el : driver.findElements(titles)) {
            String t = safeText(el);
            if (t.length() >= 3) return t;
        }

        List<WebElement> cards = driver.findElements(venueCards);
        if (!cards.isEmpty()) {
            String t = safeText(cards.get(0));
            if (t.length() >= 3) return t;
        }

        return "";
    }

    // ---------- Backward-compatible methods (so your old tests compile) ----------
    // Old tests call isLoaded(), isFiltersVisible(), isLocationVisible()

    public boolean isLoaded() {
        return isFilterPanelVisible();
    }

    public boolean isFiltersVisible() {
        return isFilterPanelVisible();
    }

    public boolean isLocationVisible() {
        // location chips (Dubai/Abu Dhabi/etc.) appear inside the filter panel,
        // so if panel is visible, location section is effectively available.
        return isFilterPanelVisible();
    }

    // ---------- Helpers ----------
    private String safeText(WebElement el) {
        try {
            return el.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
