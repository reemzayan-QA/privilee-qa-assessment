package pom;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PrivileeMapPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ✅ Staging URL (as you requested)
    public static final String BASE_URL = "https://staging-website.privilee.ae/map";

    public PrivileeMapPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // --- Stable anchors ---
    private final By filterHeader = By.xpath("//*[normalize-space()='Filter your search']");
    private final By clearFilters = By.xpath("//*[contains(normalize-space(),'Clear filters')]");
    private final By noResultsMsg = By.xpath("//*[contains(translate(.,'NO RESULTS','no results'),'no results') or " +
            "contains(translate(.,'NO VENUES','no venues'),'no venues')]");
    private final By errorMsg = By.xpath("//*[contains(translate(.,'UNABLE','unable'),'unable') or " +
            "contains(translate(.,'ERROR','error'),'error') or " +
            "contains(translate(.,'SOMETHING WENT WRONG','something went wrong'),'something went wrong')]");

    // "Result-like" (best effort)
    private final By venueCards = By.cssSelector(
            "[class*='venue'], [class*='Venue'], [class*='result'], [class*='Result'], [class*='card'], [class*='Card']"
    );

    // Marker-ish (best effort)
    private final By markerElements = By.cssSelector(
            "img[src*='googleapis.com'], img[src*='gstatic.com'], [aria-label*='marker'], [class*='marker']"
    );

    public void open() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(filterHeader),
                ExpectedConditions.presenceOfElementLocated(filterHeader)
        ));
    }

    public boolean isFilterPanelVisible() {
        return driver.findElements(filterHeader).size() > 0;
    }

    public boolean isClearFiltersVisible() {
        return driver.findElements(clearFilters).size() > 0;
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
     * ✅ For YOUR filter UI (chips/buttons with + or ×)
     * This tries to click any filter chip (prefer ones with +).
     * Returns true if we successfully toggled something.
     */
    public boolean toggleFirstAvailableFilterChip() {
        // Choose any chip that contains "+" in its text (unselected state)
        By chipWithPlus = By.xpath("//button[contains(normalize-space(.), '+')]");

        List<WebElement> chips = driver.findElements(chipWithPlus);
        for (WebElement chip : chips) {
            try {
                if (chip.isDisplayed() && chip.isEnabled()) {
                    String before = chip.getText().trim();
                    chip.click();
                    sleep(1200);
                    String after = chip.getText().trim();

                    // If text changed, we toggled (ex: + Hotel -> × Hotel)
                    if (!before.equals(after)) return true;
                }
            } catch (Exception ignored) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chip);
                    return true;
                } catch (Exception ignoredAgain) {}
            }
        }
        return false;
    }

    public void waitShortForUpdate() {
        sleep(1500);
    }

    public String getAnyVisibleVenueText() {
        // Look for any visible heading/title/name
        By titles = By.cssSelector("h1, h2, h3, [class*='title'], [class*='Title'], [class*='name'], [class*='Name']");
        for (WebElement el : driver.findElements(titles)) {
            String t = safeText(el);
            if (t.length() >= 3) return t;
        }

        // Or from a card
        List<WebElement> cards = driver.findElements(venueCards);
        if (!cards.isEmpty()) return safeText(cards.get(0));

        return "";
    }

    private String safeText(WebElement el) {
        try { return el.getText().trim(); } catch (Exception e) { return ""; }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
