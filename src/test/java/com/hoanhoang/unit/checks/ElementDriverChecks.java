package com.hoanhoang.unit.checks;

import static com.hoanhoang.unit.support.TestDoubles.mockDriver;
import static com.hoanhoang.unit.support.TestDoubles.mockElement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.Gson;
import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.pages.LoginPage;
import com.hoanhoang.pages.ShopPage;
import io.qameta.allure.Allure;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ElementDriverChecks {
    @DataProvider(name = "clickPaths")
    public Object[][] clickPaths() {
        return new Object[][] {{"wrapper"}, {"utility"}};
    }

    @Test(dataProvider = "clickPaths")
    public void clickResolvesAgainWhenTheNodeIsReplacedBeforeDispatch(String path) {
        ConfigLoader.load("dev");
        AtomicInteger lookups = new AtomicInteger();
        AtomicInteger clicks = new AtomicInteger();
        WebElement stale =
                mockElement(
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "isDisplayed", "isEnabled" -> true;
                                    case "click" ->
                                            throw new StaleElementReferenceException(
                                                    "Node replaced before click");
                                    default -> null;
                                });
        WebElement fresh =
                mockElement(
                        (proxy, method, args) -> {
                            if (method.getName().equals("isDisplayed")
                                    || method.getName().equals("isEnabled")) {
                                return true;
                            }
                            if (method.getName().equals("click")) {
                                clicks.incrementAndGet();
                            }
                            return null;
                        });
        WebDriver driver =
                mockDriver(
                        (proxy, method, args) ->
                                method.getName().equals("findElements")
                                        ? List.of(lookups.getAndIncrement() == 0 ? stale : fresh)
                                        : null,
                        JavascriptExecutor.class);
        DriverManager.setDriver(driver);
        try {
            if (path.equals("wrapper")) {
                ElementFactory.$(By.id("button")).click();
            } else {
                UiWait.click(By.id("button"));
            }
            assertThat(clicks.get()).isEqualTo(1);
            assertThat(lookups.get()).isGreaterThanOrEqualTo(2);
        } finally {
            DriverManager.quitDriver();
        }
    }

    @DataProvider(name = "filterSnapshotReads")
    public Object[][] filterSnapshotReads() {
        return new Object[][] {{"text"}, {"value"}, {"visible"}};
    }

    @Test(dataProvider = "filterSnapshotReads")
    public void filterRetriesCollectionWhenPredicateReadsAStaleNode(String read) {
        ConfigLoader.load("dev");
        AtomicInteger lookups = new AtomicInteger();
        WebElement fresh =
                mockElement(
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "isDisplayed" -> true;
                                    case "getText", "getDomProperty" -> "ready";
                                    default -> null;
                                });
        WebElement stale =
                mockElement(
                        (proxy, method, args) -> {
                            throw new StaleElementReferenceException(
                                    "Node replaced inside predicate");
                        });
        WebDriver driver =
                mockDriver(
                        (proxy, method, args) ->
                                method.getName().equals("findElements")
                                        ? List.of(lookups.getAndIncrement() == 0 ? stale : fresh)
                                        : null);
        DriverManager.setDriver(driver);
        try {
            Predicate<Element> predicate =
                    switch (read) {
                        case "text" -> e -> e.getText().equals("ready");
                        case "value" -> e -> "ready".equals(e.getValue());
                        case "visible" -> Element::isVisible;
                        default -> throw new IllegalArgumentException(read);
                    };
            ElementFactory.$$(By.id("items"))
                    .filter(predicate)
                    .first()
                    .shouldHaveExactText("ready");
            assertThat(lookups.get()).isGreaterThanOrEqualTo(2);
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Test
    public void filterSkipsHiddenNodesWithoutWaitingOnThem() {
        ConfigLoader.load("dev");
        WebElement hidden =
                mockElement(
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "isDisplayed" -> false;
                                    case "getText" -> "";
                                    default -> null;
                                });
        DriverManager.setDriver(
                mockDriver(new AtomicReference<>(List.of(hidden, mockElement("ready")))));
        try {
            ElementFactory.$$(By.id("items"))
                    .filter(e -> e.getText().equals("ready"))
                    .first()
                    .shouldHaveExactText("ready");
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Test
    public void collectionElementsResolveAgainWhenTheDomChanges() {
        ConfigLoader.load("dev");
        WebElement fresh = mockElement("updated");
        WebElement old = mockElement("old");
        AtomicReference<List<WebElement>> nodes = new AtomicReference<>(List.of(old));
        DriverManager.setDriver(mockDriver(nodes));
        try {
            var all = ElementFactory.$$(By.id("items"));
            var first = all.first();
            var last = all.last();
            var filtered = all.filter(e -> e.getText().equals("updated")).first();
            nodes.set(List.of(fresh));
            first.shouldHaveExactText("updated");
            last.shouldHaveExactText("updated");
            filtered.shouldHaveExactText("updated");
            nodes.set(List.of());
            assertThat(first.isVisible()).isFalse();
            nodes.set(List.of(mockElement("reappeared")));
            first.shouldHaveExactText("reappeared");
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Test
    public void collectionAssertionRetriesWithANewNodeAfterStaleRead() {
        ConfigLoader.load("dev");
        WebElement fresh = mockElement("ready");
        AtomicInteger reads = new AtomicInteger();
        WebElement stale =
                mockElement(
                        (proxy, method, args) -> {
                            throw new StaleElementReferenceException("DOM replaced while reading");
                        });
        WebDriver driver =
                mockDriver(
                        (proxy, method, args) ->
                                method.getName().equals("findElements")
                                        ? List.of(reads.getAndIncrement() == 0 ? stale : fresh)
                                        : null);
        DriverManager.setDriver(driver);
        try {
            ElementFactory.$$(By.id("items")).first().shouldHaveExactText("ready");
            assertThat(reads.get()).isGreaterThanOrEqualTo(2);
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Test
    public void loginStepDoesNotSerializeThePassword() {
        ConfigLoader.load("dev");
        DriverManager.setDriver(mockDriver(new AtomicReference<>(List.of(mockElement("login")))));
        String password = "framework-password-sentinel-7461";
        try {
            new LoginPage().login("framework-user", password);
            AtomicBoolean checked = new AtomicBoolean();
            Allure.getLifecycle()
                    .updateTestCase(
                            result -> {
                                assertThat(result.getSteps())
                                        .anySatisfy(
                                                step -> {
                                                    assertThat(step.getName())
                                                            .isEqualTo("Login as framework-user");
                                                    assertThat(step.getParameters()).isEmpty();
                                                });
                                assertThat(new Gson().toJson(result.getSteps()))
                                        .doesNotContain(password);
                                checked.set(true);
                            });
            assertThat(checked.get()).as("Allure test lifecycle is active").isTrue();
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Test
    public void elementChecksRetryAndReportExpectedVsActual() {
        ConfigLoader.load("dev");
        WebElement element =
                mockElement(
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "isDisplayed" -> true;
                                    case "getText" -> "THANK YOU. YOUR ORDER HAS BEEN RECEIVED.";
                                    case "getDomProperty" -> "value".equals(args[0]) ? "4" : null;
                                    default -> null;
                                });
        WebDriver driver =
                mockDriver(
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "findElements" -> List.of(element);
                                    case "getTitle" -> "Products – Demo";
                                    default -> null;
                                });
        DriverManager.setDriver(driver);
        try {
            var el = ElementFactory.$(By.id("msg"));
            el.shouldHaveText("thank you").shouldHaveValue("4");
            new ShopPage().haveTitle("Products – Demo");
            assertThatThrownBy(() -> el.shouldHaveExactText("Thank you"))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("exact text 'Thank you'")
                    .hasMessageContaining("but was 'THANK YOU. YOUR ORDER HAS BEEN RECEIVED.'");
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Test
    public void driverIsRemovedEvenWhenQuitThrows() {
        WebDriver mock =
                mockDriver(
                        (proxy, method, args) -> {
                            if (method.getName().equals("quit")) {
                                throw new IllegalStateException("simulated quit failure");
                            }
                            return null;
                        });
        DriverManager.setDriver(mock);
        assertThatThrownBy(DriverManager::quitDriver).isInstanceOf(IllegalStateException.class);
        assertThat(DriverManager.hasDriver()).isFalse();
    }

    @Test
    public void driverIsIsolatedBetweenThreads() throws InterruptedException {
        WebDriver mock = mockDriver((proxy, method, args) -> null);
        DriverManager.setDriver(mock);
        AtomicBoolean otherThreadHasDriver = new AtomicBoolean(true);
        try {
            Thread thread = new Thread(() -> otherThreadHasDriver.set(DriverManager.hasDriver()));
            thread.start();
            thread.join();
            assertThat(otherThreadHasDriver.get()).isFalse();
            assertThat(DriverManager.getDriver()).isSameAs(mock);
        } finally {
            DriverManager.quitDriver();
        }
    }
}
