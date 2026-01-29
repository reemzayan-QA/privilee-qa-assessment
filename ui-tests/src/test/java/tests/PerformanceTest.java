package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PerformanceTest extends BaseTest {
    @Test
    public void verifyPageLoadTime() {
        long start = System.currentTimeMillis();
        driver.get("https://staging-website.privilee.ae/map");
        long loadTime = System.currentTimeMillis() - start;
        Assert.assertTrue(loadTime < 6000, "Page loading is too slow");
        // 6 seconds is acceptable for Interactive Maps
    }
}
