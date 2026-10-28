# SeleniumLv2_HoanHoang

UI test automation framework for https://demo.testarchitect.com.

Java 21 · Selenium 4 · TestNG · AssertJ · Gson · Allure · Log4j2 · Maven (wrapper included)

---

## 1. Environment setup

| Requirement | Notes |
|---|---|
| JDK 21 | `java -version` must report 21. `JAVA_HOME` should point to it. |
| Maven | Not required: use the wrapper `.\mvnw.cmd` (Windows) or `./mvnw`. |
| Browser | Chrome (default), Edge or Firefox. |
| Network | Needed on first run to download dependencies and browser drivers. |

**Browser drivers are not stored in the project.** Selenium Manager (bundled with Selenium)
finds the installed browser and downloads the matching driver into `~/.cache/selenium`.
If no browser is installed it downloads "Chrome for Testing" there as well.

> **Windows Smart App Control.** If a run fails with
> `An Application Control policy has blocked this file` for `chromedriver.exe`/`chrome.exe`,
> Windows is blocking the freshly downloaded binary. Turn Smart App Control off
> (Windows Security → App & browser control) or run the browser through Selenium Grid (section 4).

Check the setup without opening a browser:

```powershell
.\mvnw.cmd "-Dtest=FrameworkTest" test
```

---

## 2. Project structure

```
src/main/java/com/hoanhoang
├── core
│   ├── config      EnvironmentConfig, ConfigLoader, JsonConfigLoader   (JSON config per environment)
│   ├── constant    FrameworkConstants                                    (resource folders)
│   ├── driver      BrowserType, DriverFactory (factory), DriverManager (ThreadLocal, one driver per thread)
│   ├── element     Element/$(), Elements/$$(), Condition                  (element wrapper + checks)
│   ├── exception   ConfigLoadException, DriverInitializationException
│   └── util        UiWait, JsonMapper                                     (shared waits/actions and Gson JSON I/O)
├── data
│   ├── model       DTOs/records: UserDto, BillingDetails, ReviewData, Product, CartItem, OrderItem, ...
│   ├── builder     BillingBuilder
│   ├── constant    PaymentMethod (enum)
│   ├── util        PriceUtils (BigDecimal money)
│   ├── TestDataManager   reads testdata/*.json into DTOs
│   └── AccountPool       one test account per running test (parallel-safe)
├── pages           Page objects: business actions and state; checks invoked by tests
│   └── components  CartTable, OrderTable, generic TableComponent, BillingForm, Payment, Review, ...
└── report
    ├── allure      AllureManager (screenshots/attachments)
    ├── listener    TestListener, RetryAnnotationTransformer, TestRetryAnalyzer
    └── logger      Log (step/debug/info/warn/error), AllureStepLogger

src/test/java/com/hoanhoang
├── tests           BaseTest, TC01 … TC10
│   └── support     PurchaseAssertions (row-by-row cart/order checks), OrderFixture (TC05 precondition)
└── unit
    ├── FrameworkTest   TestNG factory; stable Maven entry point, no browser
    ├── checks/        ConfigDataChecks, CredentialChecks, ElementDriverChecks, PurchaseChecks, RetryChecks
    └── support/       TestDoubles, RetryLifecycleProbe, CredentialSourceProbe

src/test/resources
├── config/         dev.json, prod.json, sample.json
├── testdata/       test-credentials.json, test-review.json, payment-methods.json
├── testng.xml      suite: TC01–TC10, parallel by class
└── log4j2.xml      logging configuration
```

Layering:

- **Tests** describe the scenario with business actions (`login()`, `addRandomProductToCart()`,
  `placeOrder()`) and do all checking (AssertJ / `shouldHave`).
- **Pages/components** perform actions and read state. Tests explicitly invoke check helpers
  such as `haveTitle()` and `shouldHave(...)`; business actions do not assert scenario outcomes.
- **Core** hides Selenium details: waits, element wrapper, driver lifecycle.
- **pages/support/ApplicationWait** holds this site's jQuery, WooCommerce and theme readiness rules.

---

## 3. Using the framework

### Configuration per environment

`src/test/resources/config/<env>.json`, selected with `-Denv=<env>` (default `dev`).
Loaded once per run: JSON → command-line overrides → validation → read-only snapshot.
Invalid booleans, numbers, browser names and URLs fail before any browser starts.
`sample.json` documents the full configuration. `dev.json` and `prod.json` contain the
required baseUrl and inherit other defaults from EnvironmentConfig; both currently target
the same demo site. Add keys to an environment file when its values differ from the defaults.

| Key | Meaning |
|---|---|
| `baseUrl` | Application URL |
| `browser` | `chrome` / `edge` / `firefox` |
| `headless`, `windowWidth`, `windowHeight` | Browser mode and viewport |
| `remote`, `gridUrl` | Run on Selenium Grid |
| `elementTimeout`, `actionTimeout`, `navigationTimeout` | Seconds |
| `uiTimeoutMs`, `pollingInterval` | Milliseconds (generic waits and auto-retry checks) |

All keys in this table can be overridden on the command line, e.g.
`-Dbrowser=edge -Dheadless=true`. Overrides are captured when configuration is loaded;
changing system properties afterwards does not change the running configuration.

### Driver management

`DriverFactory` builds Chrome/Edge/Firefox options (headless, window size) and returns a local
driver or a `RemoteWebDriver` for Grid. `DriverManager` keeps one driver per thread
(`ThreadLocal`), which makes TestNG parallel runs safe. `BaseTest` starts the driver before each
test and always quits it afterwards.

### Element wrapper

```java
$(By.id("username")).type("user");
$(By.name("login")).click();
$(locator).hover();  $(locator).jsClick();
$$(By.cssSelector(".product")).getTexts();

// Checks wait (auto-retry) until they pass, otherwise fail with expected vs actual:
success.confirmationMessage().shouldHaveText("Thank you");      // contains, ignore case
$(locator).shouldHaveExactText("Cart updated.");
$(quantityInput).shouldHaveValue("4");
$(checkbox).shouldHaveProp("checked", "true");
$(locator).shouldHave(Condition.text("..."));
shopPage.haveTitle(ShopPage.TITLE);
```

Actions wait until the element is visible/enabled before acting. Every check is recorded as an
Allure step and a log line, including checks that pass.
`ElementFactory` provides both `$` and `$$`. Wrapped clicks and `UiWait.click` use the same
implementation, including re-resolution when a node becomes stale before dispatch.

`$$().get(index)`, `first()`, `last()` and `filter(...)` resolve the current DOM again during
checks, so AJAX node replacement can recover. An index refers to the current position, not
the original product identity. Filtering predicates must only read state; their snapshot reads
do not start nested waits, so a stale node can return to the collection's retry loop. Wrapping a raw
`WebElement` is a snapshot and cannot recover its identity after replacement.

Tables use `TableComponent<T>`: read all rows as objects, find a row by its unique key
(product name, order number) and act inside that row, e.g. `cart.removeProduct("AirPods")`.
`setQuantity` accepts quantities >= 1; `changeQuantity` accepts only -1 or +1. To remove
an item, call `removeProduct`.
Typing a quantity selects the existing value and blurs the input. This theme schedules
UPDATE CART 1500ms after `change`, so the wrapper waits for that update instead of submitting
again. Completion checks the input's server-rendered value, quantity and subtotal.
Cart navigation and reload wait for page readiness. Cart getters read the current table
snapshot; quantity updates read the matching row once per poll. Tests call
`openShopWithEmptyCart()` explicitly for their shared shopping precondition; browser setup
in `@BeforeMethod` stays independent of scenario steps.

### Test data

- `testdata/test-credentials.json`: **fictional sample accounts**, used by browser-free checks.
  Never put working passwords or personal billing details in this committed file.
- Local credentials contain accounts `validUser1..N` (mapped to `UserDto`) and
  `billingDetails` (mapped to `BillingDetails`). Each running test leases its own account from
  `AccountPool`, so parallel tests never share a cart.
- `testdata/test-review.json`: review author (`ReviewData`).
- `testdata/payment-methods.json`: payment methods for TC03 (`PaymentMethod` enum names).
- `JsonMapper` maps JSON <-> objects with Gson.
- BillingDetails and ReviewData use record accessors such as `firstName()` and `email()`.
- `BillingBuilder` creates variants: `BillingBuilder.blank().build()` (TC07),
  `BillingBuilder.from(defaults).withEmail("...").build()`.
- Data-driven: TC03 runs once per payment method listed in `testdata/payment-methods.json`
  (`@DataProvider` → `TestDataManager.getPaymentMethods()`). Choosing which existing `PaymentMethod` values run needs no code change.

Before running UI tests on a fresh clone:

```powershell
New-Item -ItemType Directory -Force .local
Copy-Item src/test/resources/testdata/test-credentials.json .local/test-credentials.json
# Edit .local/test-credentials.json with valid demo accounts and your test billing data.
.\mvnw.cmd test
```

`.local/` is ignored by Git and is outside the resources copied into `target/test-classes`.
Credentials are loaded once, in this order:

1. `-Dcredentials.file=<path>` (path only, never pass a password on the command line).
2. Environment variable `TEST_CREDENTIALS_FILE` pointing to a JSON file.
3. `.local/test-credentials.json` in the project root.

Without these, framework unit tests use the committed fictional fixture; UI tests stop before
opening a browser and explain how to configure accounts. An explicitly configured missing or
invalid file fails instead of falling back. Each credentials file is a complete JSON document
with both accounts and `billingDetails`, following the committed sample's structure.

```powershell
.\mvnw.cmd "-Dcredentials.file=C:\private\test-credentials.json" test
```

### Logging

`Log.step / debug / info / warn / error` (Log4j2, `src/test/resources/log4j2.xml`).

- Console: INFO and above. File `logs/test.log`: DEBUG and above (overwritten each run).
- Each line shows the thread name, so parallel runs can be told apart.
- Every Allure `@Step` is logged automatically as `[STEP]`. Waits and clicks are logged at DEBUG.
- `TestListener` logs START / PASS / FAIL / SKIP with duration and the failure reason.

### Report

Allure collects results in `target/allure-results`: every page action and check as a step,
a screenshot, URL/title and page source on failure, and the random product selection
(with its seed) as an attachment. Login uses a programmatic step so the password argument
is not serialized as a step parameter. Old results generated before this fix retain their data;
generate a fresh result directory when checking or sharing the fix.
TC09 also attaches a cart AJAX trace with submitted quantities, HTTP status and detection of
WordPress error responses. The trace excludes other request fields such as credentials/nonces.

```powershell
.\mvnw.cmd allure:serve      # build and open the report
.\mvnw.cmd allure:report     # static report in target/site/allure-maven-plugin
```

---

## 4. Running tests

All commands from the project root (PowerShell). Quote `-D` arguments in PowerShell.

| Goal | Command |
|---|---|
| Whole UI suite (testng.xml, 4 classes in parallel) | `.\mvnw.cmd test` |
| Headless | `.\mvnw.cmd "-Dheadless=true" test` |
| Other browser | `.\mvnw.cmd "-Dbrowser=edge" test` |
| Other environment | `.\mvnw.cmd "-Denv=prod" test` |
| One test class | `.\mvnw.cmd "-Dtest=TC01" test` |
| Reproduce a random product choice | `.\mvnw.cmd "-Dtest=TC01" "-Drandom.seed=123" test` |
| Framework checks (no browser) | `.\mvnw.cmd "-Dtest=FrameworkTest" test` |
| Compile only | `.\mvnw.cmd -DskipTests test-compile` |

Framework listeners register through `src/test/resources/META-INF/services/org.testng.ITestNGListener`.
Failure diagnostics and retry are active for both suite runs and `-Dtest=...`; do not register
the same listeners again in suite XML.

Other options:

- `-DretryCount=1`: retry a failed test once (default `0`). A retry can place an order or post a
  review again, and the first failure stays visible in the report.
  Retry budgets reset after a successful invocation and are isolated between worker threads,
  including repeated methods and data-provider rows with identical parameters.
- `-Daccount.timeout=60`: seconds a test waits for a free account when all are leased.

### Rerun only the failed tests

After a suite run TestNG writes the failed tests to
`test-output/<suite name>/testng-failed.xml`. Copy it out of `test-output` (the next run cleans
that folder) and run it as a suite:

```powershell
Copy-Item "test-output\Selenium Test Suite\testng-failed.xml" target\rerun-failed.xml
.\mvnw.cmd "-Dsurefire.suiteXmlFiles=target\rerun-failed.xml" test
```

### Selenium Grid

```powershell
docker run -d -p 4444:4444 --shm-size=2g selenium/standalone-chrome
.\mvnw.cmd "-Dremote=true" "-DgridUrl=http://localhost:4444" test
```

### Code quality

| Check | Command |
|---|---|
| Code format (google-java-format, AOSP style) | `.\mvnw.cmd spotless:check` (fix: `.\mvnw.cmd spotless:apply`) |
| Static analysis (SpotBugs) | `.\mvnw.cmd compile spotbugs:check` |

SpotBugs runs without project-specific exclusions; the old BaseElement exclusion is no longer needed.
`.mvn/jvm.config` holds the JVM flags google-java-format needs on JDK 21.

### CI (GitHub Actions)

`.github/workflows/ci.yml`:

- **build** (every push to `main`/`develop` and every pull request): format check, compile,
  SpotBugs, `FrameworkTest`. No browser, so the result does not depend on the demo site.
- **ui** (manual: Actions → CI → Run workflow → tick "run_ui"): full suite headless on Chrome,
  then the Allure report, logs and TestNG output are uploaded as the `ui-test-results` artifact.
  It is manual because the shared demo site is intermittently unavailable.

For the manual UI job, add a repository Actions secret named `TEST_CREDENTIALS_JSON`, containing
the complete credentials JSON (accounts and billing details). The job writes it to a temporary
file outside the checkout and passes its path through `TEST_CREDENTIALS_FILE`. The normal build
job needs no secrets and uses fictional fixtures, including for pull requests from forks.

---

## 5. Test cases

| TC | Scenario | Notes |
|---|---|---|
| TC01 | Buy one random product from "Electronic Components & Supplies" | Checks cart rows, checkout rows, billing and payment on the order page |
| TC02 | Buy multiple random products | Compares complete cart and order rows |
| TC03 | Buy with each payment method | Data-driven: Bank transfer, Check payments, Cash on delivery |
| TC04 | Sort by price low→high and high→low | Also checks the Shop page title |
| TC05 | Orders appear in order history | Creates its two orders first (OrderFixture), then finds them by number |
| TC06 | Guest purchase, grid and list view | No login |
| TC07 | Mandatory billing fields left blank | Checks the error for each required field |
| TC08 | Clear shopping cart | **Fails on the demo site**: the cart still has items after "Clear shopping cart" |
| TC09 | Update quantity (+, type 4, −), then reload | Uses the theme's debounced update once per change; verifies quantity and subtotal, then persistence after reload |
| TC10 | Post a review | **Fails on the demo site**: new reviews are held for moderation, so the review count does not increase |

All test classes can run alone and in parallel. Each test starts its own browser and leases its own
account. Logged-in tests empty the account's cart before adding products. Guest tests start with a
fresh browser, so their cart is already empty.

### Known site behaviour

- The demo site intermittently returns "There has been a critical error on this website"
  (more often with parallel runs). Affected tests fail at the step that opened the page. The
  failure screenshot in Allure shows the error page.
- Product names are shown in Title Case on listing/cart pages but as stored on the order page,
  so product names are compared ignoring case.
- The site is reset daily at 23:00. Orders and reviews created by tests are removed then.
