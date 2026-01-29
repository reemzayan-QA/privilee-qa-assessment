package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class FilterPopupTest extends BaseTest {

    @Test
    public void verifyFiltersSectionVisible() {
        PrivileeMapPage page = new PrivileeMapPage(driver, wait);
        page.open();

        Assert.assertTrue(page.isFiltersVisible(),
                "Filters popup is not visible ");
    }
}
