package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.Elements;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import java.util.Random;
import java.util.Set;
import org.openqa.selenium.By;

public abstract class ProductListingPage extends BasePage {
    private static final By PRODUCT_TITLE = By.cssSelector("h2.product-title");
    private static final By SALE_PRICE = By.cssSelector(".price ins .amount");
    private static final By CURRENT_PRICE =
            By.xpath(
                    ".//*[contains(@class,'price')]//*[contains(@class,'amount')][not(ancestor::del)]");
    private static final By ADD_TO_CART_BUTTON =
            By.cssSelector(".product-details a.add_to_cart_button");
    private static final String PRODUCT_CART_BUTTON_SELECTOR =
            ".product-details a.add_to_cart_button[data-product_id='%s']";

    private final long seed = Long.getLong("random.seed", System.nanoTime());
    private final Random random = new Random(seed);

    protected abstract By addableProductCards();

    @Step("Add a random product to cart")
    public Product addRandomProductToCart() {
        return addRandomProductExcluding(Set.of());
    }

    protected Product addRandomProductExcluding(Set<String> names) {
        Elements cards = cards();
        // Only the title is needed to exclude already chosen products; skip it when nothing is
        // excluded.
        if (!names.isEmpty()) {
            cards = cards.filter(card -> !names.contains(title(card)));
        }
        if (cards.isEmpty()) {
            throw new IllegalStateException("Not enough distinct addable products");
        }
        return add(cards.get(random.nextInt(cards.size())));
    }

    @Step("Add the first product to cart")
    public Product addFirstProductToCart() {
        return add(cards().get(0));
    }

    public void waitUntilLoaded() {
        cards();
    }

    protected Elements cards() {
        Elements cards = $$(addableProductCards()).visible();
        return cards.waitUntilNotEmpty();
    }

    private static String title(Element card) {
        return card.find(PRODUCT_TITLE).getText();
    }

    protected Product readProduct(Element card) {
        String name = title(card);
        Elements prices = card.findAll(SALE_PRICE);
        if (prices.isEmpty()) {
            prices = card.findAll(CURRENT_PRICE);
        }
        Elements visiblePrices = prices.visible();
        if (visiblePrices.isEmpty()) {
            throw new IllegalStateException("No current price for " + name);
        }
        Element amount = visiblePrices.first();
        return new Product(name, PriceUtils.parse(amount.getText()));
    }

    private Product add(Element card) {
        ProductSelection selection =
                card.waitUntil(
                        "Read selected product",
                        snapshot ->
                                new ProductSelection(
                                        readProduct(snapshot),
                                        snapshot.find(ADD_TO_CART_BUTTON)
                                                .getDomAttribute("data-product_id")));
        Product product = selection.product();
        String productId = selection.id();
        if (productId == null || !productId.matches("\\d+")) {
            throw new IllegalStateException("Missing stable product ID for " + product.name());
        }
        Allure.addAttachment("Selected product (random seed " + seed + ")", product.toString());
        Element button = addToCartButton(productId);
        button.click();
        button.waitUntil(
                "Add-to-cart completed for " + product.name(),
                snapshot -> {
                    String classes = snapshot.getDomAttribute("class");
                    return classes != null
                            && classes.contains("added")
                            && !classes.contains("loading");
                });
        ApplicationWait.idle();
        return product;
    }

    private Element addToCartButton(String productId) {
        return $(By.cssSelector(PRODUCT_CART_BUTTON_SELECTOR.formatted(productId)));
    }

    private record ProductSelection(Product product, String id) {}
}
