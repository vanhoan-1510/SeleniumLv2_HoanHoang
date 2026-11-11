package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import org.openqa.selenium.By;

public class ShopPage extends ProductListingPage {
    public static final String TITLE = "Products – TestArchitect Sample Website";

    private static final By ADDABLE_PRODUCT_CARDS =
            By.cssSelector(
                    ".products-grid .content-product:has(a.add_to_cart_button), .products-list .content-product:has(a.add_to_cart_button)");
    private final Element productCard = $(By.cssSelector(".content-product"));
    private final Element sortDropdown = $(By.name("orderby"));
    private final Element gridView = $(By.cssSelector(".products-grid"));
    private final Element listView = $(By.cssSelector(".products-list"));
    private final Element listViewButton = $(By.cssSelector(".view-switcher .switch-list"));
    private final Element firstProductLink =
            $(By.cssSelector(".content-product h2.product-title a"));

    /** Current prices in display order, excluding old prices inside del. */
    private final Elements currentPrices =
            $$(
                    By.cssSelector(
                            ".products-grid .content-product .price > .amount,"
                                    + " .products-grid .content-product .price ins .amount,"
                                    + " .products-list .content-product .price > .amount,"
                                    + " .products-list .content-product .price ins .amount"));

    @Override
    protected By addableProductCards() {
        return ADDABLE_PRODUCT_CARDS;
    }

    public List<Product> addMultipleRandomProductsToCart() {
        return addMultipleRandomProductsToCart(2);
    }

    @Step("Add {count} distinct random products to cart")
    public List<Product> addMultipleRandomProductsToCart(int count) {
        if (count < 2) {
            throw new IllegalArgumentException(
                    "Multiple-product test requires at least 2 products");
        }
        List<Product> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Set<String> selected = result.stream().map(Product::name).collect(Collectors.toSet());
            result.add(addRandomProductExcluding(selected));
        }
        return List.copyOf(result);
    }

    public List<BigDecimal> getAllProductPrices() {
        List<BigDecimal> prices =
                currentPrices.waitUntil(
                        "Catalog prices readable",
                        amounts -> {
                            List<String> texts = amounts.stream().map(Element::getText).toList();
                            return texts.isEmpty()
                                    ? null
                                    : texts.stream().map(PriceUtils::parse).toList();
                        });
        Allure.addAttachment("Catalog prices (display order)", prices.toString());
        return prices;
    }

    @Step("Sort products by price: low to high")
    public void sortByPriceLowToHigh() {
        sort("price");
    }

    @Step("Sort products by price: high to low")
    public void sortByPriceHighToLow() {
        sort("price-desc");
    }

    private void sort(String value) {
        // Keep a card from the current list: it goes stale once the sorted list replaces it,
        // whether the theme reloads the page or swaps the list via AJAX.
        BooleanSupplier listReplaced = productCard.watchForReplacement();
        sortDropdown.selectByValue(value);
        productCard.waitUntil(
                "Product list replaced after sorting by " + value,
                snapshot -> listReplaced.getAsBoolean());
        ApplicationWait.idle();
        waitUntilLoaded();
    }

    public boolean isGridViewVisible() {
        return gridView.isVisible();
    }

    @Step("Switch product view to list")
    public void switchToListView() {
        listViewButton.click();
        Allure.step("Verify list view is displayed", () -> listView.waitUntilVisible());
        ApplicationWait.idle();
    }

    @Step("Open the first product detail page")
    public void clickFirstProduct() {
        firstProductLink.click();
        ApplicationWait.idle();
    }
}
