package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class OverFilteringNoVenuesTest extends BaseTest {

    @Test
    public void overFilteringShouldBeHandledGracefully() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        // Requested city is configurable, but we fall back safely if it doesn't exist
        String requestedCity = System.getenv().getOrDefault("TEST_CITY", "Abu Dhabi");

        int finalCount = page.applyFiltersToMinimizeVenues(requestedCity, "Abu Dhabi", 12);

        // Helpful (shows up in CI logs)
        System.out.println("ℹ️ Requested city: " + requestedCity);
        System.out.println("ℹ️ Actual selected city: " + page.getLastSelectedCity());
        System.out.println("ℹ️ Final CTA: " + page.getShowVenuesButtonText());

        Assert.assertTrue(finalCount >= 0,
                "Could not read 'Show N venues' CTA. The filter footer CTA may not have loaded.");

        String ctaText = page.getShowVenuesButtonText();
        Assert.assertTrue(
                ctaText.toLowerCase().contains("show") &&
                        ctaText.toLowerCase().contains("venues"),
                "CTA text did not look like 'Show N venues'. Actual: " + ctaText
        );

        // Apply filters to update results
        page.clickShowVenues();

        if (finalCount == 0) {
            // ✅ True edge case: zero venues
            Assert.assertTrue(
                    page.isZeroVenuesStateVisible() || page.isNoResultsVisible(),
                    "Expected a clear empty-state message after applying filters resulting in zero venues."
            );
        } else {
            // ✅ CI-safe validation for dynamic staging data:
            // After applying filters, we must see either data OR a clear state message.
            boolean hasAnyData =
                    page.getVenueCardCount() > 0 || page.getMarkerLikeCount() > 0;

            boolean hasStateMessage =
                    page.isLoadingVisible() ||
                            page.isNoResultsVisible() ||
                            page.isErrorVisible();

            Assert.assertTrue(
                    hasAnyData || hasStateMessage,
                    "After applying filters, the UI showed neither data nor a clear state message. This indicates a silent blank state."
            );
        }
    }
}
