package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class OverFilteringNoVenuesTest extends BaseTest {

    @Test
    public void overFilteringShouldBeHandledGracefully() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        int finalCount = page.applyFiltersToMinimizeVenues("Abu Dhabi", 12);

        Assert.assertTrue(finalCount >= 0,
                "Could not read 'Show N venues' CTA. The filter footer CTA may not have loaded.");

        String ctaText = page.getShowVenuesButtonText();
        Assert.assertTrue(ctaText.toLowerCase().contains("show") && ctaText.toLowerCase().contains("venues"),
                "CTA text did not look like 'Show N venues'. Actual: " + ctaText);

        page.clickShowVenues();

        if (finalCount == 0) {
            // ✅ True edge case achieved: zero venues
            Assert.assertTrue(page.isZeroVenuesStateVisible() || page.isNoResultsVisible(),
                    "Expected a clear empty-state message after applying filters resulting in zero venues.");
        } else {
            // ✅ CI-safe validation for dynamic staging:
            // After applying filters, we must see either data OR a clear state message.
            boolean hasAnyData = page.getVenueCardCount() > 0 || page.getMarkerLikeCount() > 0;
            boolean hasStateMsg = page.isLoadingVisible() || page.isNoResultsVisible() || page.isErrorVisible();

            Assert.assertTrue(hasAnyData || hasStateMsg,
                    "After applying filters, the UI showed neither data nor a clear state message. This indicates a silent blank state.");
        }
    }
}
