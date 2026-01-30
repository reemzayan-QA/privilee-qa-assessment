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
## First 4 scenarios are crucial UI tests, followed by Performance one
## Scenario 1
1.PageLoadTest

Feature being tested
Initial loading of the Privilee Map page.

Expected outcome
The map page loads successfully and other components in the listing page (such as the filter button, the upper header menus) are visible.

Setup / Teardown
Setup: Initialize WebDriver and navigate to https://staging-website.privilee.ae/map.

Teardown: Quit the browser after the test execution.

Why this test is important
This is the main landing page, so it has to be successfully loading, otherwise, it will be a blocker to all functions that can be accessed on this page, by all users.

## Scenario 2
2.FilterPopupTest

Feature being tested
The Filters popup page functionality on the map page.

Expected outcome
The Filters section is visible and accessible to the user, allowing filtering of venues.

Setup / Teardown

Setup: Open the Privilee Map page and wait for UI elements to render.

Teardown: Close the browser session.

Why this test is important
Filters are a core feature for users to narrow down venues. If filters are missing or broken, user experience and usability are significantly impacted.