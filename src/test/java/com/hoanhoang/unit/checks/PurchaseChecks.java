package com.hoanhoang.unit.checks;

import static com.hoanhoang.unit.support.TestDoubles.mockDriver;
import static com.hoanhoang.unit.support.TestDoubles.mockElement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.data.model.CartItem;
import com.hoanhoang.data.model.OrderItem;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.pages.components.CartTableComponent;
import com.hoanhoang.tests.support.PurchaseAssertions;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class PurchaseChecks {
    @Test
    public void unsupportedQuantityInputsFailBeforeAccessingTheBrowser() {
        CartTableComponent cart = new CartTableComponent();
        assertThatThrownBy(() -> cart.setQuantity("item", 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cart.setQuantity("item", -1))
                .isInstanceOf(IllegalArgumentException.class);
        for (int delta : new int[] {-2, 0, 2}) {
            assertThatThrownBy(() -> cart.changeQuantity("item", delta))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void typingQuantityUsesOneThemeUpdateAndWaitsForServerState() {
        ConfigLoader.load("dev");
        QuantityUpdateFixture fixture = new QuantityUpdateFixture();
        DriverManager.setDriver(fixture.driver());
        try {
            new CartTableComponent().setQuantity("A", 4);
            assertThat(fixture.serverQuantity.get()).isEqualTo(4);
            assertThat(fixture.submissions.get()).isEqualTo(1);
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Test
    public void cartAssertionsRejectPriceFromAnotherRow() {
        var expected =
                List.of(
                        new Product("A", new BigDecimal("10")),
                        new Product("B", new BigDecimal("20")));
        var actual =
                List.of(
                        new CartItem("A", new BigDecimal("20"), 1, new BigDecimal("20")),
                        new CartItem("B", new BigDecimal("10"), 1, new BigDecimal("10")));
        assertThatThrownBy(() -> PurchaseAssertions.assertCart(actual, expected))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    public void duplicateProductQuantitiesAreComparedAsOneOrderRow() {
        Product product = new Product("A", new BigDecimal("10.00"));
        PurchaseAssertions.assertOrderItems(
                List.of(new OrderItem("A", 2, new BigDecimal("20"))), List.of(product, product));
    }

    private static final class QuantityUpdateFixture {
        private final AtomicInteger clientQuantity = new AtomicInteger(1);
        private final AtomicInteger serverQuantity = new AtomicInteger(1);
        private final AtomicInteger submissions = new AtomicInteger();
        private final AtomicInteger polls = new AtomicInteger();

        private WebDriver driver() {
            WebElement row = row(input(), subtotal());
            return mockDriver(
                    (proxy, method, args) -> {
                        if (method.getName().equals("executeScript")) {
                            return !args[0].toString().contains("critical error");
                        }
                        if (method.getName().equals("findElements")) {
                            if (args[0].toString().contains("update_cart")) {
                                throw new AssertionError("Manual submit overlaps the theme update");
                            }
                            return List.of(row);
                        }
                        return null;
                    },
                    JavascriptExecutor.class);
        }

        private WebElement input() {
            return mockElement(
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "clear":
                                throw new AssertionError("Clearing triggers an extra change");
                            case "sendKeys":
                                String keys =
                                        Arrays.stream((CharSequence[]) args[0])
                                                .map(Object::toString)
                                                .collect(Collectors.joining());
                                if (keys.matches("[0-9]+")) {
                                    clientQuantity.set(Integer.parseInt(keys));
                                }
                                if (keys.equals(Keys.TAB.toString())) {
                                    submissions.incrementAndGet();
                                }
                                return null;
                            case "getDomProperty":
                                if (submissions.get() > 0 && polls.incrementAndGet() >= 2) {
                                    serverQuantity.set(clientQuantity.get());
                                }
                                return String.valueOf(clientQuantity.get());
                            case "getDomAttribute":
                                return String.valueOf(serverQuantity.get());
                            default:
                                return null;
                        }
                    });
        }

        private WebElement subtotal() {
            return mockElement(
                    (proxy, method, args) ->
                            method.getName().equals("getText")
                                    ? "$" + serverQuantity.get() * 10
                                    : null);
        }

        private WebElement row(WebElement input, WebElement subtotal) {
            return mockElement(
                    (proxy, method, args) -> {
                        if (method.getName().equals("isDisplayed")) {
                            return true;
                        }
                        if (method.getName().equals("findElement")) {
                            String locator = args[0].toString();
                            if (locator.contains("input.qty")) {
                                return input;
                            }
                            if (locator.contains("product-subtotal")) {
                                return subtotal;
                            }
                            if (locator.contains("product-price")) {
                                return mockElement("$10.00");
                            }
                            return mockElement("A");
                        }
                        return null;
                    });
        }
    }
}
