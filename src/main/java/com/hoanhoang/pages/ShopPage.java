package com.hoanhoang.pages;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class ShopPage extends ProductListingPage {
    public static final String TITLE = "Products – TestArchitect Sample Website";

    @Override
    protected By addableProductCards() {
        return By.cssSelector(
                ".products-grid .content-product:has(a.add_to_cart_button), .products-list .content-product:has(a.add_to_cart_button)");
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

    /**
     * Current price of every listed product, in display order. Regular price: ".price > .amount";
     * sale price: ".price ins .amount" (the old price inside "del" is skipped).
     */
    private static final By CURRENT_PRICES =
            By.cssSelector(
                    ".products-grid .content-product .price > .amount,"
                            + " .products-grid .content-product .price ins .amount,"
                            + " .products-list .content-product .price > .amount,"
                            + " .products-list .content-product .price ins .amount");

    public List<BigDecimal> getAllProductPrices() {
        List<BigDecimal> prices =
                UiWait.until(
                        "Catalog prices readable",
                        d -> {
                            List<String> texts = $$(CURRENT_PRICES).getTexts();
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
        WebElement firstCardBefore = UiWait.visible(By.cssSelector(".content-product"));
        new Select(UiWait.clickable(By.name("orderby"))).selectByValue(value);
        UiWait.until(
                "Product list replaced after sorting by " + value,
                ExpectedConditions.stalenessOf(firstCardBefore));
        ApplicationWait.idle();
        waitUntilLoaded();
    }

    public boolean isGridViewVisible() {
        return UiWait.isVisible(By.cssSelector(".products-grid"));
    }

    public boolean isListViewVisible() {
        return UiWait.isVisible(By.cssSelector(".products-list"));
    }

    @Step("Switch product view to list")
    public void switchToListView() {
        UiWait.click(By.cssSelector(".view-switcher .switch-list"));
        UiWait.until("List view displayed", d -> isListViewVisible());
        ApplicationWait.idle();
    }

    @Step("Open the first product detail page")
    public void clickFirstProduct() {
        UiWait.click(By.cssSelector(".content-product h2.product-title a"));
        ApplicationWait.idle();
    }
}
