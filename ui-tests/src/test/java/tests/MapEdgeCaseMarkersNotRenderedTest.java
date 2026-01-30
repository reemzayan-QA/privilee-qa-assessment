package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class MapEdgeCaseMarkersNotRenderedTest extends BaseTest {

    @Test
    public void mapShouldNotBeEmptySilently() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        page.waitShortForUpdate();

        int cards = page.getVenueCardCount();
        int markers = page.getMarkerLikeCount();

        boolean hasData = (cards > 0) || (markers > 0);
        boolean hasFeedback = page.isNoResultsVisible() || page.isErrorVisible() || page.isClearFiltersVisible();

        Assert.assertTrue(hasData || hasFeedback,
                "Edge case: map looks empty (no markers/cards) and no feedback shown (no results/error).");
    }
}
