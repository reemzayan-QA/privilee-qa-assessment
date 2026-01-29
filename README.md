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