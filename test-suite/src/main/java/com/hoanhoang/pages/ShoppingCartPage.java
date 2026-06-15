package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.RetryHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ShoppingCartPage extends BasePage {

    By productName = By.cssSelector("a.product-title");
    By productPrice = By.cssSelector("td.product-price span.woocommerce-Price-amount");
    By productQuantityField = By.cssSelector("td.product-quantity input.qty[type='number']");
    By quantityMinusButton = By.cssSelector(".minus");
    By quantityPlusButton = By.cssSelector(".plus");
    By checkoutButton = By.cssSelector("a.checkout-button");
    By clearCartButton = By.cssSelector(".clear-cart");
    By emptyCartMessage = By.xpath("//h1[contains(text(),'YOUR SHOPPING CART IS EMPTY') or contains(text(),'Cart is empty')]");
    By updateCartButton = By.cssSelector("[name='update_cart']");
    By subTotalPrice = By.cssSelector("td.product-subtotal span.woocommerce-Price-amount");
    By removeProductButton = By.cssSelector("a.remove, a.remove_from_cart_button, a[href*='remove_item'], a[href*='remove-item']");

    public void removeAllProducts() {
        waitForCartState();

        long deadline = System.currentTimeMillis() + ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs();
        while (System.currentTimeMillis() < deadline && !DriverManager.getDriver().findElements(removeProductButton).isEmpty()) {
            WebElement removeButton = DriverManager.getDriver().findElements(removeProductButton).get(0);
            ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].click();", removeButton);
            waitForCartUpdate();
        }

        clearCartBySettingQuantitiesToZero();

        verifyWithRetry(() -> {
            if (!getNonEmptyTexts(productName).isEmpty()) {
                throw new AssertionError("Cart still has products after clear");
            }
        });
    }

    private void clearCartBySettingQuantitiesToZero() {
        List<WebElement> quantityFields = DriverManager.getDriver().findElements(By.cssSelector("input.qty"));
        if (quantityFields.isEmpty()) {
            return;
        }

        ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "arguments[0].forEach(function(input) {" +
                        "input.value = '0';" +
                        "input.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "input.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "});",
                quantityFields
        );

        List<WebElement> updateButtons = DriverManager.getDriver().findElements(updateCartButton);
        if (!updateButtons.isEmpty()) {
            ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                    "arguments[0].removeAttribute('disabled'); arguments[0].click();",
                    updateButtons.get(0)
            );
            waitForCartUpdate();
        }
    }

    public String getProductName() {
        return getFirstNonEmptyText(productName, "No product name is visible in cart");
    }

    public String getProductPrice() {
        return $(productPrice).getText();
    }

    public String getProductQuantityField() {
        return $(productQuantityField).getValue();
    }

    public void verifyProductDisplayed(String expectedName, String expectedPrice, String expectedQuantity) {
        verifyWithRetry(() -> {
            assertProductNamePresent(expectedName);
            assertProductPricePresent(expectedPrice);
            assertProductQuantityPresent(expectedQuantity);
        });
    }

    public void verifyProductName(String expectedName) {
        verifyWithRetry(() -> assertProductNamePresent(expectedName));
    }

    public void verifyProductPrice(String expectedPrice) {
        verifyWithRetry(() -> assertProductPricePresent(expectedPrice));
    }

    public void verifyProductQuantity(String expectedQuantity) {
        verifyWithRetry(() -> assertProductQuantityPresent(expectedQuantity));
    }

    public void verifyProductInCart() {
        verifyWithRetry(() -> {
            if (getNonEmptyTexts(productName).isEmpty()) {
                throw new AssertionError("No product name is visible in cart");
            }
        });
    }

    public void clickQuantityMinusButton() {
        changeQuantityWithButton(quantityMinusButton, "minus");
    }

    public void clickQuantityPlusButton() {
        changeQuantityWithButton(quantityPlusButton, "plus");
    }

    public void clickCheckoutButton() {
        $(checkoutButton).click();
    }

    public List<String> getAllProductNames() {
        return getAllNonEmptyTexts(productName, "No product names are visible in cart");
    }

    public List<String> getAllProductPrices() {
        return getAllPriceTexts(productPrice, "No product prices are visible in cart");
    }

    public void clearCart() {
        clearCartUsingClearCartButton();
    }

    public void clearCartUsingClearCartButton() {
        waitForCartState();
        WebElement clearButton = findVisibleElement(clearCartButton, "Clear cart button (.clear-cart) is not visible");
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", clearButton);
        clearButton.click();
        acceptClearCartAlert();
        waitForCartUpdate();
    }

    public boolean isEmptyCartMessageVisible() {
        return $(emptyCartMessage).isVisible();
    }

    public void verifyEmptyCartMessageVisible() {
        verifyWithRetry(() -> $(emptyCartMessage).shouldBeVisible());
    }

    public void verifyCartCleared() {
        verifyWithRetry(() -> {
            List<String> products = getNonEmptyTexts(productName);
            if (!products.isEmpty()) {
                throw new AssertionError("Cart should be empty after clear cart button. Actual products: " + products);
            }
        });
    }

    public String getSubTotalPrice() {
        return $(subTotalPrice).getText();
    }

    public void verifySubTotalPrice(String expectedPrice) {
        verifyWithRetry(() -> $(subTotalPrice).shouldHaveText(expectedPrice));
    }

    public void setProductQuantity(String quantity) {
        $(productQuantityField).type(quantity);
    }

    public void clickUpdateCartButton() {
        String previousSubTotal = getOptionalSubTotalPrice();
        triggerQuantityChange();
        submitCartUpdate();
        waitForSubTotalToChange(previousSubTotal);
    }

    private void waitForCartUpdate() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for cart update", e);
        }
    }

    private void changeQuantityWithButton(By buttonLocator, String buttonName) {
        String previousQuantity = getProductQuantityField();
        String previousSubTotal = getOptionalSubTotalPrice();

        WebElement button = findVisibleElement(buttonLocator, "Quantity " + buttonName + " button is not visible");
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].click();", button);

        waitForQuantityToChange(previousQuantity);
        triggerQuantityChange();
        submitCartUpdate();
        waitForSubTotalToChange(previousSubTotal);
    }

    private void submitCartUpdate() {
        WebElement updateButton = DriverManager.getDriver().findElement(updateCartButton);
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "arguments[0].removeAttribute('disabled');" +
                        "arguments[0].disabled = false;" +
                        "arguments[0].click();",
                updateButton
        );
        waitForCartUpdate();
        waitForCartState();
    }

    private void waitForQuantityToChange(String previousQuantity) {
        RetryHandler.retryUntilTimeout(() -> {
            String currentQuantity = getProductQuantityField();
            if (currentQuantity.equals(previousQuantity)) {
                throw new AssertionError("Quantity did not change yet. Current: " + currentQuantity);
            }
        }, ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(), ConfigLoader.getConfig().getStepRetryIntervalMs());
    }

    private void waitForSubTotalToChange(String previousSubTotal) {
        if (previousSubTotal.isEmpty()) {
            return;
        }

        RetryHandler.retryUntilTimeout(() -> {
            String currentSubTotal = getOptionalSubTotalPrice();
            if (currentSubTotal.isEmpty() || currentSubTotal.equals(previousSubTotal)) {
                throw new AssertionError("Sub total did not change yet. Current: " + currentSubTotal);
            }
        }, ConfigLoader.getConfig().getPageVerificationRetryTimeoutMs(), ConfigLoader.getConfig().getStepRetryIntervalMs());
    }

    private String getOptionalSubTotalPrice() {
        return getNonEmptyTexts(subTotalPrice).stream().findFirst().orElse("");
    }

    private void waitForCartState() {
        verifyWithRetry(() -> {
            Object readyState = ((JavascriptExecutor) DriverManager.getDriver()).executeScript("return document.readyState;");
            if (!"complete".equals(readyState)) {
                throw new AssertionError("Cart page is not loaded yet");
            }
        });
    }

    private void triggerQuantityChange() {
        WebElement quantity = DriverManager.getDriver().findElement(productQuantityField);
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                quantity
        );
        waitForCartUpdate();
    }

    private WebElement findVisibleElement(By locator, String errorMessage) {
        return DriverManager.getDriver().findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new AssertionError(errorMessage));
    }

    private void acceptClearCartAlert() {
        try {
            DriverManager.getDriver().switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            throw new AssertionError("Clear cart confirmation alert was not displayed", e);
        }
    }

    private void assertProductNamePresent(String expectedName) {
        List<String> actualNames = getNonEmptyTexts(productName);
        boolean found = actualNames.stream().anyMatch(name -> name.contains(expectedName));
        if (!found) {
            throw new AssertionError("Cart should contain product name: " + expectedName + ". Actual: " + actualNames + ". " + getCartDebugInfo());
        }
    }

    private void assertProductPricePresent(String expectedPrice) {
        List<String> actualPrices = getPriceTexts(productPrice);
        boolean found = actualPrices.stream().anyMatch(price -> price.equals(expectedPrice));
        if (!found) {
            throw new AssertionError("Cart should contain product price: " + expectedPrice + ". Actual: " + actualPrices);
        }
    }

    private void assertProductQuantityPresent(String expectedQuantity) {
        List<String> actualQuantities = DriverManager.getDriver().findElements(productQuantityField).stream()
                .map(element -> element.getAttribute("value"))
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
        if (!actualQuantities.contains(expectedQuantity)) {
            throw new AssertionError("Cart should contain product quantity: " + expectedQuantity + ". Actual: " + actualQuantities);
        }
    }

    private String getFirstNonEmptyText(By locator, String emptyMessage) {
        List<String> values = getAllNonEmptyTexts(locator, emptyMessage);
        return values.get(0);
    }

    private List<String> getAllNonEmptyTexts(By locator, String emptyMessage) {
        List<List<String>> result = new ArrayList<>();
        verifyWithRetry(() -> {
            List<String> values = getNonEmptyTexts(locator);
            if (values.isEmpty()) {
                throw new AssertionError(emptyMessage);
            }
            result.clear();
            result.add(values);
        });
        return result.get(0);
    }

    private List<String> getAllPriceTexts(By locator, String emptyMessage) {
        List<List<String>> result = new ArrayList<>();
        verifyWithRetry(() -> {
            List<String> values = getPriceTexts(locator);
            if (values.isEmpty()) {
                throw new AssertionError(emptyMessage);
            }
            result.clear();
            result.add(values);
        });
        return result.get(0);
    }

    private List<String> getNonEmptyTexts(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .map(this::getElementText)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> getPriceTexts(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .flatMap(element -> extractPrices(element).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> extractPrices(WebElement element) {
        Object textContent = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "return (arguments[0].textContent || '').trim();",
                element
        );
        String text = textContent == null ? "" : textContent.toString().replaceAll("\\s+", "");
        Matcher matcher = Pattern.compile("\\$\\d[\\d,]*(?:\\.\\d{2})?").matcher(text);
        List<String> prices = new ArrayList<>();
        while (matcher.find()) {
            prices.add(matcher.group());
        }
        return prices;
    }

    private String getElementText(WebElement element) {
        String text = element.getText();
        if (text != null && !text.trim().isEmpty()) {
            return text.trim();
        }

        Object jsText = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "return (arguments[0].innerText || arguments[0].textContent || '').trim();",
                element
        );
        return jsText == null ? "" : jsText.toString().trim();
    }

    private String getCartDebugInfo() {
        String bodyText = DriverManager.getDriver().findElements(By.tagName("body")).stream()
                .findFirst()
                .map(this::getElementText)
                .orElse("");
        if (bodyText.length() > 500) {
            bodyText = bodyText.substring(0, 500);
        }

        return "URL: " + DriverManager.getDriver().getCurrentUrl()
                + ", td.product-name count: " + DriverManager.getDriver().findElements(By.cssSelector("td.product-name")).size()
                + ", a.product-title count: " + DriverManager.getDriver().findElements(By.cssSelector("a.product-title")).size()
                + ", remove count: " + DriverManager.getDriver().findElements(By.cssSelector("a.remove")).size()
                + ", first product html: " + getFirstOuterHtml(By.cssSelector("td.product-name"))
                + ", body: " + bodyText;
    }

    private String getFirstOuterHtml(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .findFirst()
                .map(element -> {
                    Object html = ((JavascriptExecutor) DriverManager.getDriver()).executeScript("return arguments[0].outerHTML;", element);
                    String value = html == null ? "" : html.toString();
                    return value.length() > 2000 ? value.substring(0, 2000) : value;
                })
                .orElse("");
    }
}
