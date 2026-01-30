package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

/**
 * Edge case: Over-filtering results in zero venues.
 * Validates both:
 * 1) Filter panel CTA shows "Show 0 venues"
 * 2) Results panel displays a clear empty state message ("0 ... venues" + apology text)
 */
public class OverFilteringNoVenuesTest extends BaseTest {

    @Test
    public void overFilteringShouldShowZeroVenuesAndEmptyStateMessage() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        // Open filters panel (if needed)
        page.openFiltersPanel();

        // Location: Abu Dhabi
        // Category: Fitness
        // Venue type/tag: Recovery
        
        page.selectFilterChip("Abu Dhabi");
        page.selectFilterChip("Fitness");
        page.selectFilterChip("Recovery");

        // Assert the filter footer shows "Show 0 venues"
        String ctaText = page.getShowVenuesButtonText();
        Assert.assertTrue(ctaText.toLowerCase().contains("show"),
                "Expected a 'Show X venues' button in filters footer, but none found.");
        Assert.assertTrue(ctaText.contains("0"),
                "Expected 'Show 0 venues' after over-filtering, but got: " + ctaText);

       
        Assert.assertTrue(page.isShowZeroVenuesVisible(),
                "Expected exact 'Show 0 venues' button to be visible after over-filtering.");

        // Apply and return to results panel
        page.clickShowVenues();

        // Assert empty state appears in results panel
        Assert.assertTrue(page.isZeroVenuesStateVisible(),
                "Expected zero-venues empty state message on results panel (e.g., '0 ... venues' and apology text).");
    }
}
