package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PerformanceTest extends BaseTest {
    @Test
    public void verifyPageLoadTime() {
        long start = System.currentTimeMillis();
        driver.get("https://staging-website.privilee.ae/map");
        long loadTime = System.currentTimeMillis() - start;
        Assert.assertTrue(loadTime < 4000, "Page loading is too slow");
        //4 to 6 seconds are acceptable for Interactive Maps, but i will pick here 4 secs
    }
}
