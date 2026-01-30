package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class FilterPanelTest extends BaseTest {

    @Test
    public void verifyFiltersSectionVisible() {
        PrivileeMapPage page = new PrivileeMapPage(driver, wait);
        page.open();

        Assert.assertTrue(page.isFiltersVisible(),
                "Filters panel is not visible ");
    }
}
