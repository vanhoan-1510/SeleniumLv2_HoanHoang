package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.RetryHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ProductListingPage extends BasePage {

    protected abstract String productCardXpath();

    protected abstract By addableProductCards();

    protected String productNameXpath() {
        return productCardXpath() + "//h2[contains(@class,'product-title')]";
    }

    protected String productPriceXpath() {
        return productCardXpath() + "//span[contains(@class,'price')]";
    }

    protected String addToCartButtonXpath() {
        return productCardXpath() + "//a[contains(.,'Add to cart')]";
    }

    public Map<String, String> addRandomProductToCart() {
        AtomicReference<Map<String, String>> product = new AtomicReference<>();
        RetryHandler.retryUntilTimeout(
                () -> product.set(addVisibleRandomProductToCart()),
                ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(),
                ConfigLoader.getConfig().getStepRetryIntervalMs(),
                this::retryPageLoad
        );
        return product.get();
    }

    public void addFirstProductToCart() {
        addProductToCart(1);
    }

    protected int getAddableProductCount() {
        verifyWithRetry(() -> {
            int totalProducts = $$(addableProductCards()).size();
            if (totalProducts == 0) {
                throw new AssertionError("No addable products are visible on product listing page");
            }
        });
        return $$(addableProductCards()).size();
    }

    protected String getProductPriceText(int index) {
        return $(By.xpath(String.format(productPriceXpath(), index))).getText();
    }

    protected String getCurrentPrice(String priceText) {
        Matcher matcher = Pattern.compile("\\$\\s?\\d[\\d,]*(?:\\.\\d{2})?").matcher(priceText);
        String currentPrice = null;
        while (matcher.find()) {
            currentPrice = matcher.group().replace("$ ", "$");
        }
        if (currentPrice == null) {
            throw new AssertionError("Could not parse product price from text: " + priceText);
        }
        return currentPrice;
    }

    private Map<String, String> addVisibleRandomProductToCart() {
        List<Integer> visibleIndexes = getVisibleAddableProductIndexes();
        int selectedIndex = visibleIndexes.get(new Random().nextInt(visibleIndexes.size()));

        String name = $(By.xpath(String.format(productNameXpath(), selectedIndex))).getText();
        String price = getCurrentPrice($(By.xpath(String.format(productPriceXpath(), selectedIndex))).getText());
        WebElement button = findVisibleElement(By.xpath(String.format(addToCartButtonXpath(), selectedIndex)));

        addToCart(button);
        waitForProductAdded(button);

        Map<String, String> product = new HashMap<>();
        product.put("name", name);
        product.put("price", price);
        return product;
    }

    private List<Integer> getVisibleAddableProductIndexes() {
        List<WebElement> cards = DriverManager.getDriver().findElements(addableProductCards());
        List<Integer> visibleIndexes = new ArrayList<>();
        for (int index = 0; index < cards.size(); index++) {
            try {
                if (cards.get(index).isDisplayed()) {
                    visibleIndexes.add(index + 1);
                }
            } catch (StaleElementReferenceException e) {
                throw new AssertionError("Product list changed while checking visible products", e);
            }
        }
        if (visibleIndexes.isEmpty()) {
            throw new AssertionError("No addable products are visible on product listing page");
        }
        return visibleIndexes;
    }

    private void addProductToCart(int index) {
        WebElement button = findVisibleElement(By.xpath(String.format(addToCartButtonXpath(), index)));
        addToCart(button);
        waitForProductAdded(button);
    }

    private WebElement findVisibleElement(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Element is not visible: " + locator));
    }

    private void addToCart(WebElement button) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].click();", button);
    }

    private void waitForProductAdded(WebElement addButton) {
        RetryHandler.retryUntilTimeout(() -> {
            try {
                String className = addButton.getAttribute("class");
                String text = addButton.getText();
                if ((className != null && className.contains("added"))
                        || (text != null && text.toLowerCase().contains("view cart"))) {
                    return;
                }
            } catch (StaleElementReferenceException ignored) {
                return;
            }

            throw new AssertionError("Product is not added to cart yet");
        }, ConfigLoader.getConfig().getStepRetryTimeoutMs(), ConfigLoader.getConfig().getStepRetryIntervalMs());
    }
}
