package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class FilterPopupTest extends BaseTest {
    @Test
    public void verifyFilterOpens() {
        PrivileeMapPage page = new PrivileeMapPage(driver, wait);
        page.open();
        page.openFilters();
        Assert.assertTrue(page.isSearchVisible(), "Filter not available");
    }
}
