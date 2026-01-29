package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class SearchVenueTest extends BaseTest {
    @Test
    public void verifySearchBoxVisible() {
        PrivileeMapPage page = new PrivileeMapPage(driver, wait);
        page.open();
        Assert.assertTrue(page.isSearchVisible(), "Search box not visible");
    }
}
