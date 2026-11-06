package com.hoanhoang.unit.checks;

import static com.hoanhoang.core.element.ElementFactory.$;
import static com.hoanhoang.core.element.ElementFactory.$$;
import static com.hoanhoang.unit.support.TestDoubles.mockDriver;
import static com.hoanhoang.unit.support.TestDoubles.mockElement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.pages.components.TableComponent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WrapperChecks {
    @BeforeMethod
    public void loadConfiguration() {
        ConfigLoader.load("dev");
    }

    @AfterMethod(alwaysRun = true)
    public void releaseDriver() {
        DriverManager.quitDriver();
    }

    @Test
    public void assertionFailurePreservesTheOriginalTimeout() {
        DriverManager.setDriver(mockDriver(new AtomicReference<>(List.of(mockElement("row")))));
        TimeoutException failure = new TimeoutException("Timed out reading the actual value");

        assertThatThrownBy(
                        () ->
                                UiWait.assertEventually(
                                        "value is ready",
                                        driver -> {
                                            throw failure;
                                        },
                                        value -> true))
                .isInstanceOf(AssertionError.class)
                .hasCause(failure);
    }

    @DataProvider(name = "unexpectedTableFailures")
    public Object[][] unexpectedTableFailures() {
        return new Object[][] {
            {new NullPointerException("Mapper bug")},
            {new IllegalArgumentException("Invalid mapper argument")},
            {new NumberFormatException("Invalid quantity")},
            {new InvalidSelectorException("Invalid row selector")},
            {new NoSuchSessionException("Browser session closed")}
        };
    }

    @Test(dataProvider = "unexpectedTableFailures")
    public void tablePropagatesUnexpectedFailuresWithoutRetry(RuntimeException failure) {
        AtomicInteger queries = new AtomicInteger();
        DriverManager.setDriver(
                mockDriver(
                        (proxy, method, args) -> {
                            if (method.getName().equals("findElements")) {
                                queries.incrementAndGet();
                                if (failure instanceof WebDriverException) {
                                    throw failure;
                                }
                                return List.of(mockElement("row"));
                            }
                            return null;
                        }));
        TableComponent<String> table =
                new TableComponent<>(
                        By.id("table"),
                        By.id("rows"),
                        row -> {
                            throw failure;
                        },
                        Element::getText);

        assertThatThrownBy(table::getRows).isSameAs(failure);
        assertThat(queries.get()).as("Unexpected errors fail on the first read").isEqualTo(1);
    }

    @Test
    public void tableRetriesExplicitlyIncompleteRows() {
        DriverManager.setDriver(mockDriver(new AtomicReference<>(List.of(mockElement("ready")))));
        AtomicInteger reads = new AtomicInteger();
        TableComponent<String> table =
                new TableComponent<>(
                        By.id("table"),
                        By.id("rows"),
                        row -> {
                            if (reads.incrementAndGet() == 1) {
                                throw new TableComponent.RowNotReadyException(
                                        "Empty price during AJAX");
                            }
                            return row.getText();
                        },
                        Element::getText);

        assertThat(table.getRows()).containsExactly("ready");
        assertThat(reads.get()).isEqualTo(2);
    }

    @Test
    public void childQueriesFollowTheirParentWhenTheDomIsReplaced() {
        var parents = new AtomicReference<>(List.of(parent(List.of(mockElement("old")))));
        DriverManager.setDriver(mockDriver(parents));
        Element child = $(By.id("parent")).find(By.id("value"));
        var children = $(By.id("parent")).findAll(By.cssSelector(".value"));

        assertThat(child.getText()).isEqualTo("old");
        parents.set(List.of(parent(List.of(mockElement("new"), mockElement("second")))));

        assertThat(child.getText()).isEqualTo("new");
        assertThat(children.map(Element::getText)).containsExactly("new", "second");
    }

    @Test
    public void aStaleChildRetriesTheWholeCollectionSnapshot() {
        AtomicInteger queries = new AtomicInteger();
        WebElement stale =
                mockElement(
                        (proxy, method, args) -> {
                            if (method.getName().equals("findElements")) {
                                throw new StaleElementReferenceException(
                                        "Row replaced during a child read");
                            }
                            return true;
                        });
        WebElement fresh = parent(List.of(mockElement("ready")));
        DriverManager.setDriver(
                mockDriver(
                        (proxy, method, args) ->
                                method.getName().equals("findElements")
                                        ? List.of(queries.getAndIncrement() == 0 ? stale : fresh)
                                        : null));

        List<String> values =
                $$(By.cssSelector(".row"))
                        .waitUntil(
                                "Read replaced rows",
                                rows ->
                                        rows.stream()
                                                .map(row -> row.find(By.id("value")).getText())
                                                .toList());

        assertThat(values).containsExactly("ready");
        assertThat(queries.get()).isEqualTo(2);
    }

    @Test
    public void snapshotCallbacksRejectActions() {
        DriverManager.setDriver(mockDriver(new AtomicReference<>(List.of(mockElement("ready")))));

        assertThatThrownBy(
                        () ->
                                $(By.id("value"))
                                        .readSnapshot(
                                                snapshot -> {
                                                    snapshot.click();
                                                    return null;
                                                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reads only");
    }

    @Test
    public void hiddenInputsCanBeReadWithoutWaitingForVisibility() {
        WebElement hidden =
                mockElement(
                        (proxy, method, args) ->
                                switch (method.getName()) {
                                    case "isDisplayed" -> false;
                                    case "isSelected" -> true;
                                    case "getDomProperty" -> "4";
                                    case "getDomAttribute" -> "payment_method_cheque";
                                    default -> null;
                                });
        DriverManager.setDriver(mockDriver(new AtomicReference<>(List.of(hidden))));
        Element input = $(By.id("payment"));

        assertThat(input.exists()).isTrue();
        assertThat(input.isVisible()).isFalse();
        assertThat(input.isSelected()).isTrue();
        assertThat(input.getDomProperty("value")).isEqualTo("4");
        assertThat(input.getDomAttribute("id")).isEqualTo("payment_method_cheque");
    }

    @Test
    public void replacementWatchKeepsTheOriginalNodeIdentity() {
        AtomicBoolean detached = new AtomicBoolean();
        WebElement original =
                mockElement(
                        (proxy, method, args) -> {
                            if (method.getName().equals("isEnabled") && detached.get()) {
                                throw new StaleElementReferenceException(
                                        "Original node was removed");
                            }
                            return true;
                        });
        var nodes = new AtomicReference<>(List.of(original));
        DriverManager.setDriver(mockDriver(nodes));
        BooleanSupplier replaced = $(By.id("node")).watchForReplacement();

        nodes.set(List.of(mockElement("replacement")));
        assertThat(replaced.getAsBoolean()).isFalse();
        detached.set(true);

        assertThat(replaced.getAsBoolean()).isTrue();
        assertThat($(By.id("node")).getText()).isEqualTo("replacement");
    }

    @Test
    public void missingSnapshotChildrenReturnToTheOuterWaitImmediately() {
        DriverManager.setDriver(mockDriver(new AtomicReference<>(List.of(parent(List.of())))));

        assertThatThrownBy(
                        () ->
                                $(By.id("row"))
                                        .readSnapshot(row -> row.find(By.id("missing")).getText()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void hiddenSelectionWaitResolvesTheCurrentNodeOnEachAttempt() {
        AtomicInteger lookups = new AtomicInteger();
        WebElement unselected = mockElement((proxy, method, args) -> false);
        WebElement selected =
                mockElement((proxy, method, args) -> method.getName().equals("isSelected"));
        DriverManager.setDriver(
                mockDriver(
                        (proxy, method, args) ->
                                method.getName().equals("findElements")
                                        ? List.of(
                                                lookups.getAndIncrement() < 2
                                                        ? unselected
                                                        : selected)
                                        : null));
        Element option = $(By.id("payment"));

        option.waitUntilPresent().waitUntilSelected();

        assertThat(option.isSelected()).isTrue();
        assertThat(option.isVisible()).isFalse();
        assertThat(lookups.get()).isGreaterThanOrEqualTo(3);
    }

    @Test
    public void collectionWaitReturnsALiveQueryAfterTheListAppears() {
        AtomicInteger lookups = new AtomicInteger();
        var nodes = new AtomicReference<>(List.<WebElement>of());
        DriverManager.setDriver(
                mockDriver(
                        (proxy, method, args) -> {
                            if (method.getName().equals("findElements")) {
                                if (lookups.incrementAndGet() == 2) {
                                    nodes.set(List.of(mockElement("ready")));
                                }
                                return nodes.get();
                            }
                            return null;
                        }));
        var rows = $$(By.id("rows")).waitUntilNotEmpty();
        nodes.set(List.of(mockElement("updated")));

        assertThat(rows.first().getText()).isEqualTo("updated");
    }

    @Test
    public void snapshotCallbacksRejectNestedElementAndCollectionWaits() {
        DriverManager.setDriver(
                mockDriver(new AtomicReference<>(List.of(parent(List.of(mockElement("ready")))))));

        assertThatThrownBy(
                        () ->
                                $(By.id("row"))
                                        .readSnapshot(
                                                snapshot ->
                                                        snapshot.waitUntil(
                                                                "Nested wait", Element::isVisible)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reads only");
        assertThatThrownBy(
                        () ->
                                $(By.id("row"))
                                        .readSnapshot(
                                                snapshot ->
                                                        snapshot.findAll(By.id("value"))
                                                                .waitUntilNotEmpty()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reads only");
    }

    private static WebElement parent(List<WebElement> children) {
        return mockElement(
                (proxy, method, args) ->
                        switch (method.getName()) {
                            case "isDisplayed", "isEnabled" -> true;
                            case "findElements" -> children;
                            default -> null;
                        });
    }
}
