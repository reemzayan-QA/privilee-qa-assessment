package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class PageLoadTest extends BaseTest {
    @Test
    public void verifyPageLoads() {
        PrivileeMapPage page = new PrivileeMapPage(driver, wait);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Privilee map page did not load successfully");
    }
}
