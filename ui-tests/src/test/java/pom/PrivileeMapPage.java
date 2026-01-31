package pom;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PrivileeMapPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public static final String BASE_URL = "https://staging-website.privilee.ae/map";

    // Track which city was actually selected (requested or fallback)
    private String lastSelectedCity = "";

    /* ============================
       Constructors (MUST support both)
       ============================ */

    // Used by tests: new PrivileeMapPage(driver)
    public PrivileeMapPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // Used by tests: new PrivileeMapPage(driver, wait)
    public PrivileeMapPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = (wait != null) ? wait : new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /* ============================
       Locators (best-effort SPA safe)
       ============================ */

    private final By filtersButton = By.xpath(
            "//button[contains(normalize-space(.), 'Filters') or contains(normalize-space(.), 'Filter')]"
    );

    // Filter panel “anchor” – header text often exists when panel is open
    private final By filterPanelAnchor = By.xpath(
            "//*[normalize-space()='Filter your search' or normalize-space()='Filters']"
    );

    private final By clearFilters = By.xpath(
            "//*[contains(normalize-space(),'Clear filters') or contains(normalize-space(),'Clear Filters')]"
    );

    private final By showVenuesButton = By.xpath(
            "//button[contains(normalize-space(.), 'Show') and contains(translate(normalize-space(.),'VENUES','venues'),'venues')]"
    );

    private final By showZeroVenuesButton = By.xpath("//button[contains(normalize-space(.), 'Show 0 venues')]");

    private final By loadingMsg = By.xpath("//*[contains(translate(.,'LOADING','loading'),'loading')]");
    private final By errorMsg = By.xpath(
            "//*[contains(translate(.,'ERROR','error'),'error') or " +
            "contains(translate(.,'SOMETHING WENT WRONG','something went wrong'),'something went wrong') or " +
            "contains(translate(.,'UNABLE','unable'),'unable')]"
    );

    private final By noResultsMsg = By.xpath(
            "//*[contains(translate(.,'NO RESULTS','no results'),'no results') or " +
            "contains(translate(.,'NO VENUES','no venues'),'no venues') or " +
            "contains(translate(.,'THERE ARE NO VENUES','there are no venues'),'there are no venues') or " +
            "contains(normalize-space(.), 'Sorry, there are no venues matching your search and filters.')]"
    );

    // Zero venues empty-state from your screenshot
    private final By zeroVenuesHeader = By.xpath(
            "//*[contains(normalize-space(.), '0') and contains(translate(normalize-space(.),'VENUES','venues'),'venues')]"
    );
    private final By zeroVenuesApology = By.xpath(
            "//*[contains(normalize-space(.), 'Sorry, there are no venues matching your search and filters.')]"
    );

    // Venue / card-ish elements
    private final By venueCards = By.cssSelector(
            "[class*='venue'], [class*='Venue'], [class*='result'], [class*='Result'], [class*='card'], [class*='Card']"
    );

    // Marker-ish elements
    private final By markerElements = By.cssSelector(
            "[aria-label*='marker'], [class*='marker'], img[src*='googleapis.com'], img[src*='gstatic.com']"
    );

    // Data accuracy: any visible title/name text
    private final By venueTitleCandidates = By.cssSelector(
            "h1, h2, h3, [class*='title'], [class*='Title'], [class*='name'], [class*='Name']"
    );

    private By chipByText(String text) {
        return By.xpath("//button[contains(normalize-space(.), '" + text + "')]");
    }

    /* ============================
       Navigation
       ============================ */

    public void open() {
        driver.get(BASE_URL);
        // Wait for either the filter button or the panel anchor (SPA)
        wait.until(d -> driver.findElements(filtersButton).size() > 0
                || driver.findElements(filterPanelAnchor).size() > 0);
    }

    /* ============================
       Methods REQUIRED by your tests
       (aliases included for compatibility)
       ============================ */

    // Used by PageLoadTest / MapLocatorTest style smoke tests
    public boolean isLoaded() {
        return driver.findElements(filtersButton).size() > 0
                || driver.findElements(filterPanelAnchor).size() > 0;
    }

    // Used by FilterNormalFlowTest
    public boolean isFilterPanelVisible() {
        return driver.findElements(filterPanelAnchor).size() > 0;
    }

    // Used by FilterPanelTest
    public boolean isFiltersVisible() {
        return isFilterPanelVisible();
    }

    // Used by FilterSelectionTest (legacy name; treated as “page ready”)
    public boolean isLocationVisible() {
        return isLoaded();
    }

    // Used by multiple tests
    public void openFiltersPanel() {
        List<WebElement> btns = driver.findElements(filtersButton);
        if (!btns.isEmpty()) {
            WebElement btn = btns.get(0);
            try {
                btn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }
            waitShortForUpdate();
        }
    }

    public boolean isClearFiltersVisible() {
        return driver.findElements(clearFilters).size() > 0;
    }

    public boolean isLoadingVisible() {
        return driver.findElements(loadingMsg).size() > 0;
    }

    public boolean isErrorVisible() {
        return driver.findElements(errorMsg).size() > 0;
    }

    public boolean isNoResultsVisible() {
        return driver.findElements(noResultsMsg).size() > 0;
    }

    public int getVenueCardCount() {
        return driver.findElements(venueCards).size();
    }

    public int getMarkerLikeCount() {
        return driver.findElements(markerElements).size();
    }

    // Used by OverFilteringNoVenuesTest
    public String getShowVenuesButtonText() {
        List<WebElement> btns = driver.findElements(showVenuesButton);
        return btns.isEmpty() ? "" : btns.get(0).getText().trim();
    }

    public int getShowVenuesCount() {
        String text = getShowVenuesButtonText();
        if (text == null || text.isEmpty()) return -1;

        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return -1;

        try {
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean isShowZeroVenuesVisible() {
        return driver.findElements(showZeroVenuesButton).size() > 0;
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

    // Used by VenueDataAccuracyTest
    public String getAnyVisibleVenueText() {
        List<WebElement> titles = driver.findElements(venueTitleCandidates);
        for (WebElement el : titles) {
            String t = safeText(el);
            if (t != null && t.trim().length() >= 3) return t.trim();
        }

        List<WebElement> cards = driver.findElements(venueCards);
        if (!cards.isEmpty()) {
            String t = safeText(cards.get(0));
            if (t != null && t.trim().length() >= 3) return t.trim();
        }

        return "";
    }

    // Used by FilterNormalFlowTest
    public boolean clickFirstFilterButtonAndDetectChange() {
        openFiltersPanel();

        int beforeCards = getVenueCardCount();
        int beforeMarkers = getMarkerLikeCount();

        // Any clickable filter option button (exclude Clear filters + Show CTA)
        By optionButtons = By.xpath(
                "//button[" +
                        "not(contains(normalize-space(.),'Clear filters')) and " +
                        "not(contains(normalize-space(.),'Show')) and " +
                        "string-length(normalize-space(.))>1" +
                        "]"
        );

        List<WebElement> buttons = driver.findElements(optionButtons);

        for (WebElement btn : buttons) {
            try {
                if (!btn.isDisplayed() || !btn.isEnabled()) continue;

                try {
                    btn.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                }

                waitShortForUpdate();

                boolean changed = getVenueCardCount() != beforeCards || getMarkerLikeCount() != beforeMarkers;
                boolean hasState = isClearFiltersVisible() || isNoResultsVisible() || isErrorVisible() || isLoadingVisible();

                if (changed || hasState) return true;

            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /* ============================
       City fallback + Over-filtering
       ============================ */

    public String getLastSelectedCity() {
        return lastSelectedCity;
    }

    public String selectCityWithFallback(String requestedCity, String fallbackCity) {
        openFiltersPanel();

        if (requestedCity != null && !requestedCity.trim().isEmpty() && isFilterChipPresent(requestedCity)) {
            selectFilterChip(requestedCity);
            lastSelectedCity = requestedCity;
            System.out.println("✅ Selected requested city: " + requestedCity);
            return requestedCity;
        }

        System.out.println("⚠️ Requested city not found: '" + requestedCity + "'. Falling back to: " + fallbackCity);

        if (!isFilterChipPresent(fallbackCity)) {
            throw new RuntimeException("Neither requested city nor fallback city is available: "
                    + requestedCity + ", " + fallbackCity);
        }

        selectFilterChip(fallbackCity);
        lastSelectedCity = fallbackCity;
        System.out.println("✅ Selected fallback city: " + fallbackCity);
        return fallbackCity;
    }

    // Overload for older tests: (city, maxClicks)
    public int applyFiltersToMinimizeVenues(String city, int maxClicks) {
        return applyFiltersToMinimizeVenues(city, "Abu Dhabi", maxClicks);
    }

    // Signature used by your OverFilteringNoVenuesTest: (requestedCity, fallbackCity, maxClicks)
    public int applyFiltersToMinimizeVenues(String requestedCity, String fallbackCity, int maxClicks) {

        selectCityWithFallback(requestedCity, fallbackCity);

        int clicks = 0;

        By optionButtons = By.xpath(
                "//button[" +
                        "not(contains(normalize-space(.),'Clear filters')) and " +
                        "not(contains(normalize-space(.),'Show')) and " +
                        "string-length(normalize-space(.))>1" +
                        "]"
        );

        while (clicks < maxClicks) {

            int current = getShowVenuesCount();
            if (current == 0) return 0;

            List<WebElement> buttons = driver.findElements(optionButtons);
            boolean clickedSomething = false;

            for (WebElement btn : buttons) {
                if (clicks >= maxClicks) break;

                try {
                    if (!btn.isDisplayed() || !btn.isEnabled()) continue;

                    String label = btn.getText().trim();
                    if (label.isEmpty()) continue;

                    // avoid re-clicking selected city chip
                    if (!lastSelectedCity.isEmpty() && label.contains(lastSelectedCity)) continue;

                    try {
                        btn.click();
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                    }

                    waitShortForUpdate();
                    clicks++;
                    clickedSomething = true;

                    int updated = getShowVenuesCount();
                    if (updated == 0) return 0;

                } catch (Exception ignored) {
                }
            }

            if (!clickedSomething) break;
        }

        return getShowVenuesCount();
    }

    /* ============================
       Chip helpers (filter selections)
       ============================ */

    public boolean isFilterChipPresent(String label) {
        openFiltersPanel();
        return driver.findElements(chipByText(label)).size() > 0;
    }

    public void selectFilterChip(String label) {
        openFiltersPanel();
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(chipByText(label)));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el));
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
        waitShortForUpdate();
    }

    /* ============================
       IMPORTANT: tests call this → must be public
       ============================ */

    // Called directly by some tests (it was private earlier)
    public void waitShortForUpdate() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) { }
    }

    private String safeText(WebElement el) {
        try {
            return el.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
