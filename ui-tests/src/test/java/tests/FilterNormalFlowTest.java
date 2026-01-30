package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class FilterNormalFlowTest extends BaseTest {

    @Test
    public void applyFilterShouldAffectResultsOrShowState() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        Assert.assertTrue(page.isFilterPanelVisible(),
                "Filter panel is not visible; cannot validate filter functionality.");

        boolean applied = page.clickFirstFilterButtonAndDetectChange();

        Assert.assertTrue(applied,
                "Could not apply any filter button OR no visible UI change detected after clicking a filter.");
    }
}
