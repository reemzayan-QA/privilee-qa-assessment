package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class MapLocatorTest extends BaseTest {
    @Test
    public void verifyMapClickable() {
        PrivileeMapPage page = new PrivileeMapPage(driver, wait);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Map not responsive");
    }
}
