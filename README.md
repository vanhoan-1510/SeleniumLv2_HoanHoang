# SeleniumLv2_HoanHoang

Java Selenium TestNG automation framework for the TestArchitect demo site.

## Stack

- Java 11
- Selenium 4
- TestNG
- Maven multi-module
- Allure report
- Log4j
- Gson test data

## Modules

```text
framework-core    Driver, config, element wrappers, retry utilities
framework-data    Test data, account pool, DTO/builder/data constants
framework-report  Allure, logging, TestNG listeners, retry analyzer
test-suite        Page objects, business flows, TestNG tests
```

## Main Features

- Local browsers: Chrome, Firefox, Edge
- Remote execution through Selenium Grid
- Headless mode with fixed viewport support
- TestNG parallel execution
- Step-level retry by timeout
- Failed-test retry by timeout
- Element wrapper style: `$(locator).click()`, `$(locator).type()`, `$(locator).shouldHaveText()`
- Allure report with screenshot on failure
- Account pool for parallel execution
- JSON environment config and test data

## Requirements

- Java 11+
- Maven 3.8+
- Chrome, Firefox, and/or Edge installed
- Optional: Allure CLI
- Optional: Selenium Grid when running `remote=true`

## Test Data

Credentials and billing data:

```text
test-suite/src/test/resources/data/test-credentials.json
```

Review data:

```text
test-suite/src/test/resources/data/test-review.json
```

The default suite is configured for 4 parallel threads. Add enough valid accounts or reduce `thread-count` in:

```text
test-suite/src/test/resources/testng.xml
```

## Run Tests

Run full suite with default config:

```powershell
mvn -pl test-suite -am test
```

Run a single test class:

```powershell
mvn -pl test-suite -am "-Dtest=TC01" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Run multiple classes:

```powershell
mvn -pl test-suite -am "-Dtest=TC01,TC02,TC03" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## Headless Local Runs

Chrome:

```powershell
mvn -pl test-suite -am "-Dbrowser=chrome" "-Dheadless=true" "-DwindowWidth=1920" "-DwindowHeight=1080" test
```

Firefox:

```powershell
mvn -pl test-suite -am "-Dbrowser=firefox" "-Dheadless=true" "-DwindowWidth=1920" "-DwindowHeight=1080" test
```

Edge with local driver:

```powershell
mvn -pl test-suite -am "-Dbrowser=edge" "-Dheadless=true" "-DwindowWidth=1920" "-DwindowHeight=1080" "-Dwebdriver.edge.driver=E:\Learn\Selenium2\SeleniumLv2_HoanHoang\drivers\edge\msedgedriver.exe" test
```

Run all 3 browsers sequentially in PowerShell:

```powershell
mvn -pl test-suite -am "-Dbrowser=chrome" "-Dheadless=true" "-DwindowWidth=1920" "-DwindowHeight=1080" test; mvn -pl test-suite -am "-Dbrowser=firefox" "-Dheadless=true" "-DwindowWidth=1920" "-DwindowHeight=1080" test; mvn -pl test-suite -am "-Dbrowser=edge" "-Dheadless=true" "-DwindowWidth=1920" "-DwindowHeight=1080" "-Dwebdriver.edge.driver=E:\Learn\Selenium2\SeleniumLv2_HoanHoang\drivers\edge\msedgedriver.exe" test
```

## Selenium Grid

Start Selenium Grid separately, then run:

```powershell
mvn -pl test-suite -am "-Dremote=true" "-DgridUrl=http://localhost:4444/wd/hub" "-Dbrowser=chrome" "-Dheadless=true" "-DwindowWidth=1920" "-DwindowHeight=1080" test
```

`remote` and `gridUrl` can also be configured in:

```text
test-suite/src/test/resources/config/dev.json
```

## Allure Report

Generate report:

```powershell
mvn -pl test-suite allure:report
```

Open generated report:

```powershell
allure open test-suite/target/site/allure-maven-plugin
```

Serve raw results:

```powershell
allure serve test-suite/target/allure-results
```

## Test Cases

| TC | Description |
| --- | --- |
| TC01 | Add and buy a product successfully |
| TC02 | Buy multiple products successfully |
| TC03 | Buy with Bank Transfer, Cheque, and COD |
| TC04 | Sort items by price |
| TC05 | Verify order history |
| TC06 | Guest checkout |
| TC07 | Required-field validation on checkout |
| TC08 | Clear shopping cart |
| TC09 | Update product quantity |
| TC10 | Submit product review |

## Known Product Defect

`TC08.userCanClearCart` is expected to fail against the current demo site because clicking `.clear-cart` and accepting the confirmation alert does not actually remove `AirPods` from the cart.

The framework intentionally keeps this as a real failure instead of forcing a false pass.

## Notes

- Local drivers under `drivers/` are ignored by Git.
- Retry attempts can make TestNG report more executed methods than the 10 logical test classes.
- TC03 uses a data provider with 3 payment methods, so it contributes 3 test invocations.
