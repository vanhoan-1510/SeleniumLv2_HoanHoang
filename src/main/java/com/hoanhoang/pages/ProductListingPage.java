package com.hoanhoang.pages;

import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.data.model.Product;
import com.hoanhoang.data.util.PriceUtils;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class ProductListingPage extends BasePage {
    protected abstract By addableProductCards();

    private final long seed = Long.getLong("random.seed", System.nanoTime());
    private final Random random = new Random(seed);

    @Step("Add a random product to cart")
    public Product addRandomProductToCart() {
        return addRandomProductExcluding(Set.of());
    }

    protected Product addRandomProductExcluding(Set<String> names) {
        List<WebElement> cards = cards();
        // Only the title is needed to exclude already chosen products; skip it when nothing is
        // excluded.
        if (!names.isEmpty()) {
            cards = cards.stream().filter(card -> !names.contains(title(card))).toList();
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

    protected List<WebElement> cards() {
        return UiWait.until(
                "Visible addable products",
                d -> {
                    List<WebElement> found = UiWait.visibleElements(addableProductCards());
                    return found.isEmpty() ? null : found;
                });
    }

    private static String title(WebElement card) {
        return card.findElement(By.cssSelector("h2.product-title")).getText().trim();
    }

    protected Product readProduct(WebElement card) {
        String name = title(card);
        List<WebElement> prices = card.findElements(By.cssSelector(".price ins .amount"));
        if (prices.isEmpty()) {
            prices =
                    card.findElements(
                            By.xpath(
                                    ".//*[contains(@class,'price')]//*[contains(@class,'amount')][not(ancestor::del)]"));
        }
        WebElement amount =
                prices.stream()
                        .filter(WebElement::isDisplayed)
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException("No current price for " + name));
        return new Product(name, PriceUtils.parse(amount.getText()));
    }

    private Product add(WebElement card) {
        Product product = readProduct(card);
        WebElement button = card.findElement(By.cssSelector("a.add_to_cart_button"));
        String productId = button.getAttribute("data-product_id");
        if (productId == null || !productId.matches("\\d+")) {
            throw new IllegalStateException("Missing stable product ID for " + product.name());
        }
        By stableButton =
                By.cssSelector("a.add_to_cart_button[data-product_id='" + productId + "']");
        Allure.addAttachment("Selected product (random seed " + seed + ")", product.toString());
        UiWait.click(stableButton);
        UiWait.until(
                "Add-to-cart completed for " + product.name(),
                d ->
                        d.findElements(stableButton).stream()
                                .anyMatch(
                                        e -> {
                                            String classes = e.getAttribute("class");
                                            return classes != null
                                                    && classes.contains("added")
                                                    && !classes.contains("loading");
                                        }));
        ApplicationWait.idle();
        return product;
    }
}
