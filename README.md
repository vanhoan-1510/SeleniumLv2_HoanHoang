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
| Network | Full Internet access is required throughout every UI test run, including access to the demo website and its resources. Maven dependencies, browser drivers and reporting tools also need network access when downloaded or updated. |
| Test accounts | Valid demo accounts in `.local/test-credentials.json` or an external credentials file; see [Test data](#test-data). |

**Keep full network access enabled for the entire UI run**, including retries. Tests navigate the
live demo website, submit forms and wait for AJAX responses. Downloading dependencies and drivers
once does not make the UI suite work offline. In an environment that restricts network access,
allow the test process and browser to reach the demo site and its required resources for the
whole run. When using Selenium Grid, the browser running on the Grid also needs this access.

**Browser drivers are not stored in the project.** Selenium Manager (bundled with Selenium)
finds the installed browser and downloads the matching driver into `~/.cache/selenium`.
If no browser is installed it downloads "Chrome for Testing" there as well.
Driver downloads can be needed again after a browser update or when the local cache is missing.

> **Windows Smart App Control.** If a run fails with
> `An Application Control policy has blocked this file` for `chromedriver.exe`/`chrome.exe`,
> Windows is blocking the freshly downloaded binary. Turn Smart App Control off
> (Windows Security → App & browser control) or run the browser through Selenium Grid (section 4).

Check the framework without opening a browser or contacting the demo website:

```powershell
.\mvnw.cmd "-Dtest=FrameworkTest" test
```

These checks use fictional fixtures when no local credentials are configured. Maven and its
plugins can still require network access to resolve dependencies and tools.

---

## 2. Project structure

```
src/main/java/com/hoanhoang
├── core
│   ├── config      EnvironmentConfig, ConfigLoader, JsonConfigLoader   (JSON config per environment)
│   ├── constant    FrameworkConstants                                    (resource folders)
│   ├── driver      BrowserType, DriverFactory, DriverManager (ThreadLocal), DriverUtils (browser operations)
│   ├── element     Element/$(), Elements/$$(), Condition                  (element wrapper + checks)
│   ├── exception   ConfigLoadException, DriverInitializationException
│   └── util        WebDriverWaitHelper, JsonMapper                         (wait engine, JSON I/O)
├── data
│   ├── model       DTOs/records: UserDto, BillingDetails, ReviewData, Product, CartItem, OrderItem, ...
│   ├── builder     BillingBuilder
│   ├── constant    PaymentMethod (enum)
│   ├── util        PriceUtils (BigDecimal money)
│   ├── TestDataManager   reads private credentials and classpath fixtures into DTOs
│   └── AccountPool       one test account per running test (parallel-safe)
├── pages           Page objects: business actions and state; checks invoked by tests
│   └── components  CartTable, OrderTable, generic TableComponent, BillingForm, Payment, Review, ...
└── report
    ├── allure      AllureManager (screenshots/attachments)
    ├── listener    TestListener, RetryAnnotationTransformer, TestRetryAnalyzer
    └── logger      Log (step/debug/info/warn/error), AllureStepLogger

src/test/java/com/hoanhoang
├── tests           BaseTest and functional test classes (SingleProductPurchaseTest, CartQuantityTest, ...)
│   └── support     PurchaseAssertions, OrderFixture, TestSession, SuiteConfiguration
└── unit
    ├── FrameworkTest   TestNG factory; stable Maven entry point, no browser
    ├── checks/        ConfigDataChecks, CredentialChecks, ElementDriverChecks, PurchaseChecks, RetryChecks, WrapperChecks
    └── support/       TestDoubles, RetryLifecycleProbe, CredentialSourceProbe, ChildProcesses

src/test/resources
├── config/         dev.json, prod.json, sample.json
├── testdata/       test-credentials.json, test-review.json, payment-methods.json
├── testng.xml      suite: 10 functional test classes, parallel by class
└── log4j2.xml      logging configuration
```

Layering:

- **Tests** describe the scenario with business actions (`login()`, `addRandomProductToCart()`,
  `placeOrder()`) and do all checking (AssertJ / `shouldHave`).
  Steps are grouped by preparation, action and verification. Longer assertion blocks live in
  named helpers inside the test class; reusable cart/order checks live in `PurchaseAssertions`.
  Test classes, including `BaseTest` and unit checks, contain no `try` blocks.
  Resource management belongs in support fixtures. Teardown uses
  `@AfterMethod(alwaysRun = true)`; `TestSession` releases accounts even if browser shutdown
  fails. Scenario methods contain direct business actions and assertions. Expected exceptions use
  assertions such as `assertThatThrownBy(...)`.
- **Pages/components** perform actions and read state. Tests explicitly invoke check helpers
  such as `haveTitle()` and `shouldHave(...)`; business actions do not assert scenario outcomes.
- **Core** hides Selenium details: waits, element wrapper, driver lifecycle.
- **DriverManager** manages the per-thread driver lifecycle. `DriverUtils` opens URLs, refreshes
  the current page and executes browser scripts using that thread's driver.
- **Element wrappers** own DOM polling and element-specific JavaScript actions, delegating script
  execution to `DriverUtils.executeScript(...)`. `WebDriverWaitHelper` supplies the wait engine
  and browser/page waits.
- **Pages/components** use `Element` and `Elements` for DOM access. Raw `WebElement`,
  child-element lookup, Selenium `Select` and node-staleness checks stay inside core.
  Browser navigation and application-specific JavaScript readiness checks remain separate.
- **pages/support/ApplicationWait** holds this site's jQuery, WooCommerce and theme readiness rules.
  Readiness timeouts include document state, active jQuery requests, visible blockers and recent
  checkout-update HTTP statuses/timings in the exception and an Allure attachment. The original
  timeout remains the cause; a resource-timing status of 0/null is unavailable, not a success.
  Login and checkout waits also detect WordPress critical-error pages before waiting for missing UI.

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
driver or a `RemoteWebDriver` for Grid. Chrome and Edge share Chromium argument configuration.
`DriverManager` keeps one driver per thread
(`ThreadLocal`), which makes TestNG parallel runs safe. `BaseTest` starts the driver before each
test and always quits it afterwards. Headed runs maximize the browser window, with the configured
size as a fallback; headless runs use `windowWidth` and `windowHeight`.

### Element wrapper

Keep fixed element fields at the top of their owning page or component. Actions refer to
these fields instead of declaring locators inline:

```java
private final Element usernameInput = $(By.id("username"));
private final Element loginButton = $(By.name("login"));

public void enterUsername(String username) {
    usernameInput.type(username);
}
```

Use `private static final By` for child selectors applied within a product card or table row.
Keep dynamic selector templates at the top too, with named helpers such as `ratingStar(rating)`
and `addToCartButton(productId)`. Keep each locator in its owning class.

Wrapper API examples (selector construction shown explicitly):

```java
$(By.id("username")).type("user");
$(By.name("login")).click();
$(locator).hover();  $(locator).jsClick();
$$(By.cssSelector(".product")).map(Element::getText);
$(By.name("orderby")).selectByValue("price");
$(By.id("billing_email")).blur();

// Child queries resolve their parent again when the DOM changes:
Element quantity = $(By.cssSelector(".cart_item")).find(By.cssSelector("input.qty"));
quantity.replaceTextAndBlur("4");

// Checks wait (auto-retry) until they pass, otherwise fail with expected vs actual:
success.confirmationMessage().shouldHaveText("Thank you");      // contains, ignore case
$(locator).shouldHaveExactText("Cart updated.");
$(quantityInput).shouldHaveValue("4");
$(locator).shouldHave(Condition.text("..."));
shopPage.haveTitle(ShopPage.TITLE);
```

Actions wait until the element is visible/enabled before acting. Every check is recorded as an
Allure step and a log line, including checks that pass.
`ElementFactory` provides both `$` and `$$`. Wrapped clicks scroll the target into view and
resolve it again when a node becomes stale before dispatch.

`waitUntilPresent()` and `waitUntilSelected()` support hidden inputs; `waitUntilVisible()`
waits for a visible target. Collections provide `waitUntilNotEmpty()` and
`waitUntilSizeLessThan(count)` and return live queries for subsequent actions.
For custom DOM conditions, `Element.waitUntil(description, condition)` and
`Elements.waitUntil(description, condition)` resolve fresh read-only snapshots per polling
attempt. A null result or `false` retries; any other result completes the wait. These custom
waits use `uiTimeoutMs` and `pollingInterval`. Snapshot callbacks cannot perform actions or
start nested waits. Use immediate reads inside an existing browser/page wait.

`$$().get(index)`, `first()`, `last()` and `filter(...)` resolve the current DOM again during
checks, so AJAX node replacement can recover. An index refers to the current position, not
the original product identity. Filtering predicates must only read state; their snapshot reads
do not start nested waits, so a stale node can return to the collection's retry loop.

`find` and `findAll` keep the parent query, so nested lookups also follow DOM replacement.
`getDomAttribute` and `getDomProperty` can read elements that exist but are hidden, such as payment
radio buttons and the review rating field. `exists`, `isVisible` and `isSelected` are immediate
state checks; actions still require an appropriate visible/enabled target.

`readSnapshot` and `Elements.map` read fixed nodes within one polling attempt. Their callbacks
are read-only: child getters do not start their own waits, and actions are rejected. Use them
inside an outer wait when AJAX can replace a row, and do not retain snapshot elements between
attempts. `watchForReplacement` captures the original node and returns a condition that detects
its removal, even when the locator already matches a new node.

Tables use `TableComponent<T>`: read all rows as objects, find a row by its unique key
(product name, order number) and act inside that row.
`setQuantity` accepts quantities >= 1; `changeQuantity` accepts only -1 or +1.
`ShoppingCartPage.removeAllProducts()` empties the cart for test preparation.
Typing a quantity selects the existing value, blurs the input and clicks UPDATE CART,
as required by TC09. Plus/minus changes use the theme's debounced auto-update (1500ms).
Both paths wait for the original cart row to be replaced and for application readiness;
the test then verifies quantity and subtotal independently of the completion wait.
Blur uses the DOM `blur()` operation; no keyboard navigation key is sent. Billing fields also
use this wrapper operation after typing. Quantity replacement avoids `clear()` to preserve a
single change event when replacing the value.
Cart navigation and reload wait for page readiness. Cart getters read the current table
snapshot; quantity updates watch the original row for replacement. Tests call
`openShopWithEmptyCart()` explicitly for their shared shopping precondition; browser setup
in `@BeforeMethod` stays independent of scenario steps.

### Test data

- `testdata/test-credentials.json`: **fictional sample accounts**, used by browser-free checks.
  Never put working passwords or personal billing details in this committed file.
- Local credentials contain accounts `validUser1..N` (mapped to `UserDto`) and
  `billingDetails` (mapped to `BillingDetails`). Each running test leases its own account from
  `AccountPool`, so parallel tests never share a cart.
- `testdata/test-review.json`: review author (`ReviewData`).
- `testdata/payment-methods.json`: payment methods for `PaymentMethodPurchaseTest` (`PaymentMethod` enum names).
- `JsonMapper` maps JSON <-> objects with Gson.
- BillingDetails and ReviewData use record accessors such as `firstName()` and `email()`.
- `BillingBuilder` creates variants: `BillingBuilder.blank().build()` (`CheckoutValidationTest`),
  `BillingBuilder.from(defaults).withEmail("...").build()`.
- Data-driven: `PaymentMethodPurchaseTest` runs once per payment method listed in `testdata/payment-methods.json`
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
a screenshot, URL/title, page source and browser-console warnings/errors (when supported) on
failure, and the random product selection
(with its seed) as an attachment. Login uses a programmatic step so the password argument
is not serialized as a step parameter. Old results generated before this fix retain their data;
generate a fresh result directory when checking or sharing the fix.

```powershell
.\mvnw.cmd allure:serve      # build and open the report
.\mvnw.cmd allure:report     # static report in target/site/allure-maven-plugin
```

---

## 4. Running tests

All commands from the project root (PowerShell). Quote `-D` arguments in PowerShell.
Before running UI tests, configure valid accounts as described in [Test data](#test-data)
and keep full Internet access available for the complete run.

| Goal | Command |
|---|---|
| Whole UI suite (up to 4 classes in parallel, capped by account count) | `.\mvnw.cmd test` |
| Whole UI suite with visible browsers | `.\mvnw.cmd test "-Dheadless=false"` |
| Headless | `.\mvnw.cmd "-Dheadless=true" test` |
| Visible browsers, retry each failed test up to 2 times | `.\mvnw.cmd test "-Dheadless=false" "-DretryCount=2"` |
| Other browser | `.\mvnw.cmd "-Dbrowser=edge" test` |
| Other environment | `.\mvnw.cmd "-Denv=prod" test` |
| One test class | `.\mvnw.cmd "-Dtest=SingleProductPurchaseTest" test` |
| Reproduce a random product choice | `.\mvnw.cmd "-Dtest=SingleProductPurchaseTest" "-Drandom.seed=123" test` |
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

`retryCount` counts additional attempts: `2` permits up to 3 attempts for each invocation
(the first run plus 2 retries). Successful invocations do not retry. TestNG marks failed attempts
scheduled for retry as `SKIP` with `retried="true"`; inspect the TestNG/Allure details when Maven's
per-run summary is unclear. The build still fails if an invocation exhausts its retry budget.

### Rerun only the failed tests

After a suite run TestNG writes the failed tests to
`test-output/<suite name>/testng-failed.xml`. Copy it out of `test-output` to preserve the failed-test
list before another run can overwrite it, then run the copy as a suite:

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

Test classes are named after their functional area and scenario. The original case IDs remain
in test descriptions and this table for traceability; use the class name with Maven's `-Dtest`.

| ID | Test class | Scenario | Notes |
|---|---|---|---|
| TC01 | `SingleProductPurchaseTest` | Buy one random product from "Electronic Components & Supplies" | Checks cart rows, checkout rows, billing and payment on the order page |
| TC02 | `MultipleProductPurchaseTest` | Buy multiple random products | Compares complete cart and order rows |
| TC03 | `PaymentMethodPurchaseTest` | Buy with each payment method | Data-driven: Bank transfer, Check payments, Cash on delivery |
| TC04 | `ProductSortingTest` | Sort by price low→high and high→low | Also checks the Shop page title |
| TC05 | `OrderHistoryTest` | Orders appear in order history | Creates its two orders first (OrderFixture), then finds them by number |
| TC06 | `GuestCheckoutTest` | Guest purchase, grid and list view | No login |
| TC07 | `CheckoutValidationTest` | Mandatory billing fields left blank | Checks messages and field highlights separately using `BillingFormComponent.Field`, keeping HTML IDs in the page/component; includes postcode when required by the current form |
| TC08 | `CartClearTest` | Clear shopping cart | Verifies the clear button removes every row and shows the empty-cart message; runs have failed when items remained |
| TC09 | `CartQuantityTest` | Update quantity (+, type 4 and click UPDATE CART, −), then reload | Waits for the cart row to be replaced; assertions compare quantity and subtotal, then persistence after reload |
| TC10 | `ProductReviewTest` | Post a review | Reopens Reviews after submission and verifies the unique submitted comment is visible with the correct stars; does not check review count or require approval |

All test classes can run alone and in parallel. Each invocation, including a retry, starts a fresh
browser. Logged-in tests lease an account from `AccountPool` and release it during cleanup.
`SuiteConfiguration` runs before TestNG creates workers and caps UI suite, test and data-provider
thread counts at the account count. Startup logs show requested and effective suite/provider
counts without account identities. Explicitly lower limits are preserved; browser-free suites
keep their own concurrency settings.
Shopping scenarios explicitly empty the account's cart before adding products. Guest tests use
their fresh browser session and do not lease a login account; the UI suite setup still requires
the credentials file, including billing data.

### Known site behaviour

- The demo site intermittently returns "There has been a critical error on this website"
  or HTTP 500 responses. WordPress critical-error pages are detected by `ApplicationWait` and
  reported as failures with diagnostics. Server errors are recorded for investigation; they
  are not converted into passing tests. Retries can recover from a transient response, but a
  persistent failure still fails the build.
- Product names are shown in Title Case on listing/cart pages but as stored on the order page,
  so product names are compared ignoring case.
- This is a shared demo environment: existing data and availability can change between runs.
  `OrderHistoryTest` creates its own orders, and `ProductReviewTest` uses a unique comment to identify its submitted review.
