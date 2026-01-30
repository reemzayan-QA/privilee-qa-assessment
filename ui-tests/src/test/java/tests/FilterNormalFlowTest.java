package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class FilterNormalFlowTest extends BaseTest {

    @Test
    public void applyFilterShouldChangeResultsOrShowState() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        Assert.assertTrue(page.isFilterPanelVisible(), "Filter panel is not visible; cannot validate filter functionality.");

        int beforeCards = page.getVenueCardCount();
        int beforeMarkers = page.getMarkerLikeCount();

        boolean toggled = page.toggleFirstAvailableFilterChip();
        Assert.assertTrue(toggled, "Could not toggle any filter chip (+ ...).");

        page.waitShortForUpdate();

        int afterCards = page.getVenueCardCount();
        int afterMarkers = page.getMarkerLikeCount();

        boolean changed = (afterCards != beforeCards) || (afterMarkers != beforeMarkers);
        boolean hasState = page.isNoResultsVisible() || page.isErrorVisible();

        Assert.assertTrue(changed || hasState,
                "After applying a filter, results did not change and no 'no results/error' state appeared.");
    }
}
