package tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import pom.PrivileeMapPage;

public class VenueDataAccuracyTest extends BaseTest {

    @Test
    public void venueDataShouldNotBeBlank() {
        PrivileeMapPage page = new PrivileeMapPage(driver);
        page.open();

        page.waitShortForUpdate();

        String text = page.getAnyVisibleVenueText();
        Assert.assertTrue(text != null && text.trim().length() >= 3,
                "Data accuracy issue: no meaningful venue/title text found (blank or missing data).");
    }
}
