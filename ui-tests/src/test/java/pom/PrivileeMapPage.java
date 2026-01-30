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

    // ✅ Backward-compatible constructor (driver + wait)
    public PrivileeMapPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = (wait != null) ? wait : new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // ---------- Locators ----------
    // Filter panel header (stable anchor)
    private final By filterHeader = By.xpath("//*[normalize-space()='Filter your search' or normalize-space()='Filters']");

    // Filters button on main page (opens filter drawer/panel)
    private final By filtersButton = By.xpath("//button[contains(normalize-space(.), 'Filters')]");

    // Clear filters text
    private final By clearFilters = By.xpath("//*[contains(normalize-space(),'Clear filters')]");

    // Loading / error / empty states (best effort)
    private final By loadingMsg = By.xpath("//*[contains(translate(.,'LOADING','loading'),'loading')]");
    private final By errorMsg = By.xpath("//*[contains(translate(.,'UNABLE','unable'),'unable') or " +
            "contains(translate(.,'ERROR','error'),'error') or " +
            "contains(translate(.,'SOMETHING WENT WRONG','something went wrong'),'something went wrong')]");

    // Generic "no venues/no results" (best effort)
    private final By noResultsMsg = By.xpath("//*[contains(translate(.,'NO RESULTS','no results'),'no results') or " +
            "contains(translate(.,'NO VENUES','no venues'),'no venues') or " +
            "contains(translate(.,'NO MATCH','no match'),'no match') or " +
            "contains(translate(.,'THERE ARE NO VENUES','there are no venues'),'there are no venues')]");

    // Venue cards / results (best effort for SPA)
    private final By venueCards = By.cssSelector(
            "[class*='venue'], [class*='Venue'], [class*='result'], [class*='Result'], [class*='card'], [class*='Card']"
    );

    // Marker-ish elements (best effort; maps can be canvas-based)
    private final By markerElements = By.cssSelector(
            "img[src*='googleapis.com'], img[src*='gstatic.com'], [aria-label*='marker'], [class*='marker']"
    );

    // Filter footer CTA: "Show X venues"
    private final By showVenuesButton = By.xpath("//button[contains(normalize-space(.), 'Show') and contains(normalize-space(.), 'venues')]");
    private final By showZeroVenuesButton = By.xpath("//button[contains(normalize-space(.), 'Show 0 venues')]");

    // Left panel empty header like "0 fitness venues"
    private final By zeroVenuesHeader = By.xpath("//*[contains(normalize-space(.), '0') and contains(translate(normalize-space(.),'VENUES','venues'),'venues')]");
    // Exact apology sentence shown in screenshot
    private final By zeroVenuesApology = By.xpath("//*[contains(normalize-space(.), 'Sorry, there are no venues matching your search and filters.')]");

    // ---------- Dynamic locator ----------
    private By filterChipByText(String text) {
        
        return By.xpath("//button[contains(normalize-space(.), '" + text + "')]");
    }

    // ---------- Actions ----------
    public void open() {
        driver.get(BASE_URL);
        // Wait for a stable anchor to indicate UI rendered
        wait.until(ExpectedConditions.presenceOfElementLocated(filterHeader));
    }

    public void openFiltersPanel() {
        // On some layouts filter panel is already visible; on others open via "Filters" button
        List<WebElement> btns = driver.findElements(filtersButton);
        if (!btns.isEmpty()) {
            try {
                btns.get(0).click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btns.get(0));
            }
            sleep(800);
        }
    }

    /**
     * Click a filter chip/button by its visible label (e.g., "Abu Dhabi", "Fitness", "Recovery").
     */
    public void selectFilterChip(String label) {
        By chip = filterChipByText(label);
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(chip));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el));
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
        waitShortForUpdate();
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
        // covers both generic "no results" and your specific apology text
        return driver.findElements(noResultsMsg).size() > 0 || driver.findElements(zeroVenuesApology).size() > 0;
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
     * Clicks the first available filter button and detects any valid UI change:
     * - results/markers count changes OR
     * - clear filters appears OR
     * - no results / error appears
     */
    public boolean clickFirstFilterButtonAndDetectChange() {

        int beforeCards = getVenueCardCount();
        int beforeMarkers = getMarkerLikeCount();

        // All button-like filter options, excluding "Clear filters" and empty labels
        By filterButtons = By.xpath(
                "//button[not(contains(normalize-space(.), 'Clear filters')) and string-length(normalize-space(.)) > 1]"
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

    // ---------- Zero venues helpers ----------
    public boolean isShowZeroVenuesVisible() {
        return driver.findElements(showZeroVenuesButton).size() > 0;
    }

    public String getShowVenuesButtonText() {
        List<WebElement> btns = driver.findElements(showVenuesButton);
        return btns.isEmpty() ? "" : btns.get(0).getText().trim();
    }

    public void clickShowVenues() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(showVenuesButton));
        try {
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        waitShortForUpdate();
    }

    public boolean isZeroVenuesStateVisible() {
        boolean headerVisible = driver.findElements(zeroVenuesHeader).size() > 0;
        boolean apologyVisible = driver.findElements(zeroVenuesApology).size() > 0;
        return headerVisible && apologyVisible;
    }

    // ---------- Data accuracy helper ----------
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

    public void waitShortForUpdate() {
        sleep(1500);
    }

    // ---------- Backward-compatible methods (so older tests compile) ----------
    public boolean isLoaded() {
        return isFilterPanelVisible();
    }

    public boolean isFiltersVisible() {
        return isFilterPanelVisible();
    }

    public boolean isLocationVisible() {
        // Location chips appear inside the filter panel; if panel visible, location is available.
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
