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

    // Stores which city we actually selected (requested or fallback)
    private String lastSelectedCity = "";

    // ---------- Constructors ----------
    public PrivileeMapPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // Backward-compatible constructor
    public PrivileeMapPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = (wait != null) ? wait : new WebDriverWait(driver, Duration.ofSeconds(20));
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

    private final By venueCards = By.cssSelector(
            "[class*='venue'], [class*='Venue'], [class*='result'], [class*='Result'], [class*='card'], [class*='Card']"
    );

    private final By markerElements = By.cssSelector(
            "img[src*='googleapis.com'], img[src*='gstatic.com'], [aria-label*='marker'], [class*='marker']"
    );

    private final By showVenuesButton = By.xpath("//button[contains(normalize-space(.), 'Show') and contains(normalize-space(.), 'venues')]");
    private final By showZeroVenuesButton = By.xpath("//button[contains(normalize-space(.), 'Show 0 venues')]");

    private final By zeroVenuesHeader = By.xpath("//*[contains(normalize-space(.), '0') and contains(translate(normalize-space(.),'VENUES','venues'),'venues')]");
    private final By zeroVenuesApology = By.xpath("//*[contains(normalize-space(.), 'Sorry, there are no venues matching your search and filters.')]");

    // ---------- Dynamic locator ----------
    private By filterChipByText(String text) {
        return By.xpath("//button[contains(normalize-space(.), '" + text + "')]");
    }

    // ---------- Navigation ----------
    public void open() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(filterHeader));
    }

    public void openFiltersPanel() {
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

    // ---------- Filter actions ----------
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

    /**
     * - Try selecting requested city
     * - If not found, select fallback city
     * - Stores the selected city in lastSelectedCity
     */
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

    // ---------- “Show venues” CTA helpers ----------
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

    /**
     * Applies filters until venue count is minimized.
     * Uses requested city with a safe fallback to avoid invalid config values.
     *
     * @param requestedCity City (from TEST_CITY)
     * @param fallbackCity  Safe fallback (e.g. "Abu Dhabi")
     * @param maxClicks     Safety cap to avoid endless loops
     * @return final "Show N venues" count (0..N) or -1 if CTA not found
     */
    public int applyFiltersToMinimizeVenues(String requestedCity, String fallbackCity, int maxClicks) {

        // Select city (requested or fallback) safely
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

                    // Avoid re-clicking the selected city label again
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

    // ---------- Utility ----------
    public void waitShortForUpdate() {
        sleep(1500);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
