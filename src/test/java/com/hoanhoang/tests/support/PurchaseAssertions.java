package com.hoanhoang.tests.support;

import com.hoanhoang.data.model.CartItem;
import com.hoanhoang.data.model.OrderItem;
import com.hoanhoang.data.model.Product;
import io.qameta.allure.Step;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;

/** Test-layer assertions compare complete rows, including duplicate-product quantities. */
public final class PurchaseAssertions {
    private PurchaseAssertions() {}

    @Step("Compare selected products with cart rows")
    public static void assertCart(List<CartItem> actual, List<Product> expected) {
        Map<String, List<Product>> groups = groupByProductName(expected);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("Cart row count").hasSize(groups.size());
        for (List<Product> selectedProducts : groups.values()) {
            Product expectedProduct = selectedProducts.getFirst();
            String name = expectedProduct.name();
            var matches =
                    actual.stream().filter(item -> item.name().equalsIgnoreCase(name)).toList();
            softly.assertThat(matches)
                    .as("Cart row for %s (actual cart rows: %s)", name, actual)
                    .hasSize(1);
            if (matches.size() != 1) {
                continue;
            }
            CartItem row = matches.get(0);
            softly.assertThat(row.unitPrice())
                    .as("%s unit price", row.name())
                    .isEqualByComparingTo(expectedProduct.price());
            softly.assertThat(row.quantity())
                    .as("%s quantity", row.name())
                    .isEqualTo(selectedProducts.size());
            softly.assertThat(row.subtotal())
                    .as("%s subtotal", row.name())
                    .isEqualByComparingTo(totalPrice(selectedProducts));
        }
        softly.assertAll();
    }

    @Step("Compare selected products with order rows")
    public static void assertOrderItems(List<OrderItem> actual, List<Product> expected) {
        Map<String, List<Product>> groups = groupByProductName(expected);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual).as("Order row count").hasSize(groups.size());
        for (List<Product> selectedProducts : groups.values()) {
            String name = selectedProducts.getFirst().name();
            var matches =
                    actual.stream().filter(item -> item.name().equalsIgnoreCase(name)).toList();
            softly.assertThat(matches)
                    .as("Order row for %s (actual order rows: %s)", name, actual)
                    .hasSize(1);
            if (matches.size() != 1) {
                continue;
            }
            OrderItem item = matches.get(0);
            softly.assertThat(item.quantity())
                    .as("%s order quantity", item.name())
                    .isEqualTo(selectedProducts.size());
            softly.assertThat(item.total())
                    .as("%s line total", item.name())
                    .isEqualByComparingTo(totalPrice(selectedProducts));
        }
        softly.assertAll();
    }

    /**
     * Groups by case-insensitive name: the site renders listing/cart titles in Title Case but order
     * rows as stored.
     */
    private static Map<String, List<Product>> groupByProductName(List<Product> products) {
        return products.stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.name().toLowerCase(Locale.ROOT),
                                LinkedHashMap::new,
                                Collectors.toList()));
    }

    private static BigDecimal totalPrice(List<Product> products) {
        return products.stream().map(Product::price).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
