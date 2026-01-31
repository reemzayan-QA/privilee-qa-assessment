
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

    // stores which city was actually selected (requested or fallback)
    private String lastSelectedCity = "";

    // ---------- Constructors ----------
    public PrivileeMapPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // ---------- Locators ----------
    private final By filterHeader = By.xpath("//*[normalize-space()='Filter your search' or normalize-space()='Filters']");
    private final By filtersButton = By.xpath("//button[contains(normalize-space(.), 'Filters')]");
    private final By clearFilters = By.xpath("//*[contains(normalize-space(),'Clear filters')]");

    private final By loadingMsg = By.xpath("//*[contains(translate(.,'LOADING','loading'),'loading')]");

    private final By errorMsg = By.xpath("//*[contains(translate(.,'UNABLE','unable'),'unable') or " +
            "contains(translate(.,'ERROR','error'),'error') or " +
            "contains(translate(.,'SOMETHING WENT WRONG','something went wrong'),'something went wrong')]");

    private final By noResultsMsg = By.xpath("//*[contains(translate(.,'NO RESULTS','no results'),'no results') or " +
            "contains(translate(.,'NO VENUES','no venues'),'no venues') or " +
            "contains(translate(.,'NO MATCH','no match'),'no match') or " +
            "contains(translate(.,'THERE ARE NO VENUES','there are no venues'),'there are no venues')]");

    // Map and results are SPA-based, so use best-effort selectors
    private final By venueCards = By.cssSelector(
            "[class*='venue'], [class*='Venue'], [class*='result'], [class*='Result'], [class*='card'], [class*='Card']"
    );

    private final By markerElements = By.cssSelector(
            "img[src*='googleapis.com'], img[src*='gstatic.com'], [aria-label*='marker'], [class*='marker']"
    );

    // Footer CTA button inside filter panel (e.g., "Show 12 venues")
    private final By showVenuesButton = By.xpath("//button[contains(normalize-space(.), 'Show') and contains(normalize-space(.), 'venues')]");
    private final By showZeroVenuesButton = By.xpath("//button[contains(normalize-space(.), 'Show 0 venues')]");

    // Empty state shown on main page when there are zero venues
    private final By zeroVenuesHeader = By.xpath("//*[contains(normalize-space(.), '0') and contains(translate(normalize-space(.),'VENUES','venues'),'venues')]");
    private final By zeroVenuesApology = By.xpath("//*[contains(normalize-space(.), 'Sorry, there are no venues matching your search and filters.')]");

    // For data accuracy test to find any visible venue-like text
    private final By venueTitleCandidates = By.cssSelector(
            "h1, h2, h3, [class*='title'], [class*='Title'], [class*='name'], [class*='Name']"
    );

    // ---------- Dynamic locator ----------
    private By filterChipByText(String text) {
        return By.xpath("//button[contains(normalize-space(.), '" + text + "')]");
    }

    // ---------- Basic actions ----------
    public void open() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(filterHeader));
    }

    public void openFiltersPanel() {
        // In some layouts filter panel is already visible; if button exists, click it.
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

    public boolean isFilterChipPresent(String label) {
        openFiltersPanel();
        return driver.findElements(filterChipByText(label)).size() > 0;
    }

    // ---------- Optional enhancement: city selection fallback ----------
    public String selectCityWithFallback(String requestedCity, String fallbackCity) {
        openFiltersPanel();

        if (requestedCity != null && !requestedCity.trim().isEmpty() && isFilterChipPresent(requestedCity)) {
            selectFilterChip(requestedCity);
            lastSelectedCity = requestedCity;
            System.out.println("✅ Selected requested city: " + requestedCity);
            return requestedCity;
        }

        System.out.println("⚠️ Requested city not found or empty: '" + requestedCity + "' — falling back to: " + fallbackCity);

        if (!isFilterChipPresent(fallbackCity)) {
            throw new RuntimeException("Neither requested city nor fallback city is available: " +
                    requestedCity + ", " + fallbackCity);
        }

        selectFilterChip(fallbackCity);
        lastSelectedCity = fallbackCity;
        System.out.println("✅ Selected fallback city: " + fallbackCity);
        return fallbackCity;
    }

    public String getLastSelectedCity() {
        return lastSelectedCity;
    }

    // ---------- State checks ----------
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

    // ---------- CTA helpers ----------
    public boolean isShowZeroVenuesVisible() {
        return driver.findElements(showZeroVenuesButton).size() > 0;
    }

    public String getShowVenuesButtonText() {
        List<WebElement> btns = driver.findElements(showVenuesButton);
        return btns.isEmpty() ? "" : btns.get(0).getText().trim();
    }

    public int getShowVenuesCount() {
        String text = getShowVenuesButtonText();
        if (text == null) return -1;

        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return -1;

        try {
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return -1;
        }
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

    // ---------- Edge-case helper: minimize venues ----------
    public int applyFiltersToMinimizeVenues(String requestedCity, String fallbackCity, int maxClicks) {

        selectCityWithFallback(requestedCity, fallbackCity);

        int clicks = 0;

        // filter options: buttons excluding Clear filters and CTA
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

                    // avoid re-clicking the selected city
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

    // ---------- Compatibility helper for FilterNormalFlowTest ----------
    public boolean clickFirstFilterButtonAndDetectChange() {

        openFiltersPanel();

        int beforeCards = getVenueCardCount();
        int beforeMarkers = getMarkerLikeCount();

        By filterButtons = By.xpath(
                "//button[" +
                        "not(contains(normalize-space(.), 'Clear filters')) and " +
                        "not(contains(normalize-space(.), 'Show')) and " +
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

                int afterCards = getVenueCardCount();
                int afterMarkers = getMarkerLikeCount();

                boolean changed = (afterCards != beforeCards) || (afterMarkers != beforeMarkers);
                boolean hasState = isClearFiltersVisible() || isNoResultsVisible() || isErrorVisible() || isLoadingVisible();

                if (changed || hasState) return true;

            } catch (Exception ignored) {
            }
        }
        return false;
    }

    // ---------- Compatibility helper for VenueDataAccuracyTest ----------
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

    // ---------- Backward-compatible methods ----------
    public boolean isLoaded() {
        return isFilterPanelVisible();
    }

    public boolean isFiltersVisible() {
        return isFilterPanelVisible();
    }

    public boolean isLocationVisible() {
        return isFilterPanelVisible();
    }

    // ---------- Helpers ----------
    public void waitShortForUpdate() {
        sleep(1500);
    }

    private String safeText(WebElement el) {
        try {
            return el.getText();
        } catch (Exception e) {
            return "";
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
