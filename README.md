## privilee-qa-assessment

## UI Automation
Tool: Selenium WebDriver + Java + TestNG  
Target: https://staging-website.privilee.ae/map

### UI Local Runs
cd ui-tests  
mvn clean test

Screenshots on failure:
artifacts/selenium-reports/screenshots/

---

## API Automation
Tool: Postman + Newman  
Base: https://gorest.co.in/public/v2

### API Local Runs
npm install -g newman newman-reporter-htmlextra

newman run ./collections/gorest_public_v2.postman_collection.json \
-e ./collections/gorest_env.json \
--reporters cli,junit,htmlextra \
--reporter-junit-export ./artifacts/newman/results.xml \
--reporter-htmlextra-export ./artifacts/newman/report.html

---

## CI
GitHub Actions runs both UI and API tests and uploads reports as artifacts.

---
## Details of the Test Scenarios
1) Page Load Smoke Test

Test: PageLoadTest

Feature being tested
Privilee Map page loads successfully on staging.

Expected outcome
The page loads and all page components 

Setup / Teardown

Setup: Start WebDriver, navigate to https://staging-website.privilee.ae/map

Teardown: Quit the browser (and capture screenshot on failure if your BaseTest does that)

Why this test is important
If the page can’t load, all other functionality is blocked. This is your basic availability/health check.

2) Filters Panel Visibility Test

Test: FilterPanelTest (renamed from FilterPopupTest)

Feature being tested
Filter panel UI availability.

Expected outcome
The filter panel is visible and accessible to the user.

Setup / Teardown

Setup: Open the staging map page and wait for UI to render

Teardown: Close the browser

Why this test is important
Filters are a primary entry point for user navigation. If the filter panel is missing or hidden, the experience breaks immediately.

3) Search / Location Controls Visibility Test

Test: SearchVenueTest

Feature being tested
Presence of search/location controls within the filter area (e.g., location chips Dubai/Abu Dhabi or search-related UI).

Expected outcome
Search/location controls are visible and usable.

Setup / Teardown

Setup: Open staging map page and wait for the filter section to load

Teardown: Close the browser

Why this test is important
Search/location is a core discovery path. If it disappears due to UI regression, users can’t find venues efficiently.

4) Map Rendering Test

Test: MapLocatorTest

Feature being tested
Map component rendering on the page.

Expected outcome
The map container renders (map is present and not blank due to missing main UI).

Setup / Teardown

Setup: Open staging map page and wait for initial render

Teardown: Close the browser

Why this test is important
The map is the central component. If it fails to render, the product loses its main purpose.

5) Page Load Performance Test

Test: PerformanceTest

Feature being tested
Initial page load performance (basic performance regression check).

Expected outcome
The page reaches a usable state within the defined threshold (your test’s configured limit).

Setup / Teardown

Setup: Start timer, navigate to staging map page, stop timer when key UI anchor is ready

Teardown: Close browser

Why this test is important
Performance regressions are common in SPAs (especially map pages). This gives early warning if load time becomes unacceptable.

6) Filter Functionality – Normal User Flow

Test: FilterNormalFlowTest

Feature being tested
Functional filtering behavior when a user clicks a filter button/chip.

Expected outcome
After clicking a filter button:

Results/markers change OR

a valid state appears (e.g., “Clear filters”, “No results”, or an error message)

Setup / Teardown

Setup: Open staging map page, ensure filter panel is visible

Teardown: Close browser (optional: reset filters if you want clean state for future tests)

Why this test is important
This proves real functionality: filters aren’t just present; they trigger behavior and update what the user sees.

7) Map Edge Case – Markers/Data Not Rendered

Test: MapEdgeCaseMarkersNotRenderedTest

Feature being tested
Edge-case handling when the map loads but venue data (markers/cards) does not render.

Expected outcome
The application does not silently show an empty map. It should show one of:

venue data (markers/cards) OR

a clear feedback state (loading/no-results/error)

Setup / Teardown

Setup: Open staging map page and wait briefly for data render

Teardown: Close browser

Why this test is important
This is a high-impact real-world risk for map-based SPAs: users interpret a blank map as a broken product. The test ensures graceful handling instead of silent failure.

8) Venue Data Accuracy Test

Test: VenueDataAccuracyTest

Feature being tested
Accuracy/completeness of venue data displayed to the user (name/title not empty).

Expected outcome
At least one visible venue/title text is non-empty and meaningful (not blank).

Setup / Teardown

Setup: Open staging map page, wait for UI/data render, read visible venue/title text

Teardown: Close browser

Why this test is important
Even if the UI loads, bad data (blank names/titles) makes the product unusable. This catches data mapping/content issues early.