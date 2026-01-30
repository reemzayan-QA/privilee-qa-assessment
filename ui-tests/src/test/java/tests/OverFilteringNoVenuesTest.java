package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

/**
 * Edge case: Over-filtering should be handled gracefully.
 *
 * Primary goal: Reach "Show 0 venues" and validate empty state.
 * Fallback goal (CI-safe): If 0 cannot be reached due to dynamic staging data,
 * ensure we minimized results to a very low number and UI remains consistent.
 */
public class OverFilteringNoVenuesTest extends BaseTest {

    @Test
    public void overFilteringShouldBeHandledGracefully() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        // Try to reduce venues as much as possible within a safe cap.
        int finalCount = page.applyFiltersToMinimizeVenues("Abu Dhabi", 12);

        Assert.assertTrue(finalCount >= 0,
                "Could not read 'Show N venues' CTA. The filters footer may not have loaded.");

        if (finalCount == 0) {
            // ✅ True edge case achieved
            Assert.assertTrue(page.isShowZeroVenuesVisible() || page.getShowVenuesButtonText().contains("0"),
                    "Expected 'Show 0 venues' CTA to be visible once over-filtered.");

            page.clickShowVenues();

            Assert.assertTrue(page.isZeroVenuesStateVisible() || page.isNoResultsVisible(),
                    "Expected a clear empty-state message after applying filters resulting in zero venues.");

        } else {
            // ✅ CI-safe fallback: still validate over-filtering reduced results significantly
            Assert.assertTrue(finalCount <= 3,
                    "Could not reach 0 venues; expected heavily filtered count <= 3, but got: " + finalCount);

            // Apply and ensure we don't show empty-state incorrectly
            page.clickShowVenues();
            Assert.assertFalse(page.isZeroVenuesStateVisible(),
                    "Unexpected zero-venues empty state shown when CTA count was: " + finalCount);
        }
    }
}
