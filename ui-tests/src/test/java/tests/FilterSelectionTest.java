package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class FilterSelectionTest extends BaseTest {

    @Test
    public void verifyLocationFilterVisible() {
        PrivileeMapPage page = new PrivileeMapPage(driver, wait);
        page.open();

        Assert.assertTrue(page.isLocationVisible(),
                "Location filter section is not visible");
    }
}
