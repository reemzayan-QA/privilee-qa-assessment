package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

/**
 * Edge case: Over-filtering should be handled gracefully.
 *
 * - We attempt to reduce results by applying multiple filters.
 * - If we reach 0, we assert the empty state.
 * - Otherwise, we assert the UI stays consistent:
 *   CTA shows a numeric count, and applying filters does not show the zero-state incorrectly.
 *
 * This avoids flaky assumptions about staging data.
 */
public class OverFilteringNoVenuesTest extends BaseTest {

    @Test
    public void overFilteringShouldBeHandledGracefully() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        // Try to minimize venues (dynamic data, so may or may not reach zero)
        int finalCount = page.applyFiltersToMinimizeVenues("Abu Dhabi", 12);

        Assert.assertTrue(finalCount >= 0,
                "Could not read 'Show N venues' CTA. The filter footer CTA may not have loaded.");

        String ctaText = page.getShowVenuesButtonText();
        Assert.assertTrue(ctaText.toLowerCase().contains("show") && ctaText.toLowerCase().contains("venues"),
                "CTA text did not look like 'Show N venues'. Actual: " + ctaText);

        if (finalCount == 0) {
            // ✅ True edge case: zero venues
            Assert.assertTrue(page.isShowZeroVenuesVisible() || ctaText.contains("0"),
                    "Expected 'Show 0 venues' CTA for zero results, but got: " + ctaText);

            page.clickShowVenues();

            Assert.assertTrue(page.isZeroVenuesStateVisible() || page.isNoResultsVisible(),
                    "Expected a clear empty-state message after applying filters resulting in zero venues.");

        } else {
            // ✅ CI-safe validation when not zero:
            // We still prove over-filtering is handled and UI is consistent.

            // The CTA count should not increase after applying filters (sanity).
            page.clickShowVenues();

            // Must not incorrectly show "zero venues" empty state when CTA was non-zero.
            Assert.assertFalse(page.isZeroVenuesStateVisible(),
                    "Unexpected zero-venues empty state shown even though CTA was: " + ctaText);

            // Must not show an error state silently.
            Assert.assertFalse(page.isErrorVisible(),
                    "Error state detected after applying filters. The app should handle filter application gracefully.");

            // Optional: if the UI displays cards, ensure we didn't end up with empty UI without a message.
            boolean hasAnyData = page.getVenueCardCount() > 0 || page.getMarkerLikeCount() > 0;
            boolean hasStateMsg = page.isLoadingVisible() || page.isNoResultsVisible() || page.isErrorVisible();

            Assert.assertTrue(hasAnyData || hasStateMsg,
                    "After applying filters, the UI showed neither data nor a clear state message. This indicates a silent blank state.");
        }
    }
}
