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
## The following are 4 UI/smoke tests, 1 performance test, 2 functional tests covering normal and edge flows, and 1 data accuracy test.

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

Test: FilterSelectionTest

Feature being tested
Presence of filter selection buttons/ controls within the filter page , such city so on.

Expected outcome
Filter selection buttons/ controls are visible and usable.

Setup / Teardown

Setup: Open staging map page and wait for the filter page to load

Teardown: Close the browser

Why this test is important
Filter selection buttons/ controls are essential to be displayed, otherwise users can’t find venues efficiently and the filter section is broken.

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
The page reaches a usable state within the defined threshold.

Setup / Teardown

Setup: Start timer, navigate to staging map page, stop timer when key UI anchor is ready

Teardown: Close browser

Why this test is important
Performance is a main test to be carried out, especially for landing pages containing SPA Maps. This gives early warning if load time becomes unacceptable.

6) Filter Functionality – Normal User Flow

Test: FilterNormalFlowTest

Feature being tested
Functional filtering behavior when a user clicks a filter button/control.

Expected outcome
After clicking a filter button:

Results/markers change OR a valid state appears (e.g., “Clear filters”, “Show 0 Venues”, or an error message)

Setup / Teardown

Setup: Open staging map page, ensure filter panel is visible

Teardown: Close browser and reset filters.

Why this test is important
This proves real functionality: filters aren’t just present; they trigger behavior and update what the user sees.

7) Map Edge Case – Markers/Data Not Rendered

Test: MapEdgeCaseMarkersNotRenderedTest

Feature being tested
Edge-case handling when the map loads but venue data (markers/cards) does not render.

Expected outcome
The application does not silently show an empty map. It should show one of:

venue data (markers/cards) OR a clear feedback state (loading/no-results/error)

Setup / Teardown

Setup: Open staging map page and wait briefly for data render

Teardown: Close browser

Why this test is important
This is a high-impact real-world risk for map-based SPAs: users interpret a blank map as a broken product. The test ensures user friendly handling instead of unexplained failures.

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
Even if the UI loads, bad data (blank names/titles) makes the product unusable and hursta the UX part of the journey. This catches data mapping/content issues early.

9) OverFilteringNoVenuesTest – Over-filtering Edge Case Handling

Feature being tested
Application behavior when a user applies a large number of restrictive filters on the map.

Expected outcome

If filters result in zero venues, the application displays a clear empty-state message (e.g. “Sorry, there are no venues matching your search and filters”).

If zero venues cannot be reached due to dynamic staging data, the application still displays a valid UI state (venues, loading indicator, no-results message, or error message), and does not end up in a silent blank screen.

Setup / Teardown

Setup: Open the Privilee map page, open the filter panel, apply a location filter and progressively apply additional filter options.

Teardown: Browser is closed automatically after test execution.

Why this test is important
Map-based SPAs are especially prone to edge cases when filters become overly restrictive. This test validates that the application handles extreme filtering gracefully and always communicates a clear state to the user, rather than failing silently or appearing broken. The test is designed to be resilient to changing staging data and suitable for CI environments.