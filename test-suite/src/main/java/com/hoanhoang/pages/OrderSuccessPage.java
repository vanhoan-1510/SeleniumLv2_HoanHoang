package com.hoanhoang.pages;

import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderSuccessPage extends BasePage {

    By statusMessage = By.xpath("//p[contains(@class,'thankyou-order-received')]");
    By orderNumber = By.xpath("//li[contains(.,'Order number')]//strong");
    By orderDate = By.xpath("//li[contains(.,'Date')]//strong");
    By orderTotal = By.xpath("//li[contains(.,'Total')]//strong");
    By orderPaymentMethod = By.xpath("//tr[th[normalize-space()='Payment method:']]/td");
    By orderProductName = By.xpath("//td[contains(@class,'product-name')]/a");
    By orderProductQty = By.xpath("//td[contains(@class,'product-name')]//strong");
    By orderProductPrice = By.xpath("//td[contains(@class,'product-total')]");
    By billingName = By.cssSelector(".woocommerce-customer-details address, .woocommerce-column--billing-address address, address");

    public boolean isOrderSuccessHeadingVisible() {
        return $(statusMessage).isVisible();
    }

    public void verifyOrderSuccessHeadingVisible() {
        verifyWithRetry(() -> {
            if (!$(statusMessage).isVisible()) {
                throw new AssertionError("Order success message is not visible. " + getPageDebugInfo());
            }
        });
    }

    public void verifyOrderConfirmationMessage() {
        $(statusMessage).shouldContainTextIgnoringCase("Thank you");
    }

    public String getOrderedProductName() {
        return $(orderProductName).getText();
    }

    public String getOrderedProductPrice() {
        return $(orderProductPrice).getText();
    }

    public List<String> getAllOrderedProductNames() {
        return $$(orderProductName).getTexts();
    }

    public List<String> getAllOrderedProductPrices() {
        return $$(orderProductPrice).getTexts();
    }

    public String getBillingName() {
        return getVisibleTexts(billingName).stream().findFirst().orElse("");
    }

    public void verifyOrderedProduct(String expectedName, String expectedPrice) {
        verifyOrderedProductName(expectedName);
        verifyOrderedProductPrice(expectedPrice);
    }

    public void verifyOrderedProductName(String expectedName) {
        verifyWithRetry(() -> {
            List<String> actualNames = getAllOrderedProductNames();
            if (!containsIgnoringCase(actualNames, expectedName)) {
                throw new AssertionError("Order should contain product name: " + expectedName + ". Actual: " + actualNames);
            }
        });
    }

    public void verifyOrderedProductPrice(String expectedPrice) {
        verifyWithRetry(() -> {
            List<String> actualPrices = getAllOrderedProductPrices();
            if (!actualPrices.contains(expectedPrice)) {
                throw new AssertionError("Order should contain product price: " + expectedPrice + ". Actual: " + actualPrices);
            }
        });
    }

    public void verifyOrderedProducts(List<Map<String, String>> expectedProducts) {
        verifyWithRetry(() -> {
            List<String> actualNames = getAllOrderedProductNames();
            List<String> actualPrices = getAllOrderedProductPrices();
            Map<String, BigDecimal> expectedTotalsByName = getExpectedTotalsByName(expectedProducts);

            for (Map.Entry<String, BigDecimal> expectedProduct : expectedTotalsByName.entrySet()) {
                String expectedName = expectedProduct.getKey();
                String expectedPrice = formatPrice(expectedProduct.getValue());
                if (!containsIgnoringCase(actualNames, expectedName)) {
                    throw new AssertionError("Order should contain product name: " + expectedName + ". Actual: " + actualNames);
                }
                if (!actualPrices.contains(expectedPrice)) {
                    throw new AssertionError("Order should contain product price: " + expectedPrice + ". Actual: " + actualPrices);
                }
            }
        });
    }

    public void verifyBillingName(String expectedFirstName, String expectedLastName) {
        verifyWithRetry(() -> {
            List<String> addresses = getVisibleTexts(billingName);
            boolean found = addresses.stream().anyMatch(address ->
                    address.contains(expectedFirstName) && address.contains(expectedLastName));
            if (!found) {
                throw new AssertionError("Billing address should contain: "
                        + expectedFirstName + " " + expectedLastName
                        + ". Actual addresses: " + addresses + ". " + getPageDebugInfo());
            }
        });
    }

    public void verifyOrderDetailsContain(String expectedName, String expectedPrice, String expectedFirstName, String expectedLastName) {
        verifyOrderedProductName(expectedName);
        verifyOrderedProductPrice(expectedPrice);
        verifyBillingName(expectedFirstName, expectedLastName);
    }

    public String getStatusMessage() {
        return $(statusMessage).getText();
    }

    public String getOrderNumber() {
        return $(orderNumber).getText();
    }

    public String getOrderDate() {
        return $(orderDate).getText();
    }

    public String getOrderTotal() {
        return $(orderTotal).getText();
    }

    public String getPaymentMethod() {
        return $(orderPaymentMethod).getText();
    }

    public String getOrderProductQty() {
        return $(orderProductQty).getText();
    }

    private String normalizePrice(String priceText) {
        String[] prices = priceText.trim().split("\\$");
        return "$" + prices[prices.length - 1].trim();
    }

    private Map<String, BigDecimal> getExpectedTotalsByName(List<Map<String, String>> expectedProducts) {
        Map<String, BigDecimal> expectedTotalsByName = new LinkedHashMap<>();
        for (Map<String, String> product : expectedProducts) {
            String name = product.get("name");
            BigDecimal price = parsePrice(product.get("price"));
            expectedTotalsByName.merge(name, price, BigDecimal::add);
        }
        return expectedTotalsByName;
    }

    private BigDecimal parsePrice(String priceText) {
        Matcher matcher = Pattern.compile("\\$\\s?([\\d,]+(?:\\.\\d{2})?)").matcher(priceText);
        BigDecimal currentPrice = null;
        while (matcher.find()) {
            currentPrice = new BigDecimal(matcher.group(1).replace(",", ""));
        }
        if (currentPrice == null) {
            throw new AssertionError("Could not parse product price from text: " + priceText);
        }
        return currentPrice;
    }

    private String formatPrice(BigDecimal price) {
        DecimalFormat formatter = new DecimalFormat("$#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return formatter.format(price);
    }

    private boolean containsIgnoringCase(List<String> actualValues, String expectedValue) {
        return actualValues.stream().anyMatch(actual -> actual.equalsIgnoreCase(expectedValue));
    }

    private String getPageDebugInfo() {
        String bodyText = DriverManager.getDriver().findElements(By.tagName("body")).stream()
                .findFirst()
                .map(WebElement::getText)
                .orElse("");
        if (bodyText.length() > 500) {
            bodyText = bodyText.substring(0, 500);
        }
        return "URL: " + DriverManager.getDriver().getCurrentUrl() + ", body: " + bodyText;
    }

    private List<String> getVisibleTexts(By locator) {
        return DriverManager.getDriver().findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .flatMap(element -> extractPricesOrText(element).stream())
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> extractPricesOrText(WebElement element) {
        String text = getElementText(element);
        Matcher matcher = Pattern.compile("\\$\\s?\\d[\\d,]*(?:\\.\\d{2})?").matcher(text);
        List<String> prices = new ArrayList<>();
        while (matcher.find()) {
            prices.add(matcher.group().replace("$ ", "$"));
        }
        if (!prices.isEmpty()) {
            return prices;
        }
        return java.util.Collections.singletonList(text.trim());
    }

    private String getElementText(WebElement element) {
        String text = element.getText();
        if (text != null && !text.trim().isEmpty()) {
            return text.trim();
        }

        Object jsText = ((org.openqa.selenium.JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "return (arguments[0].innerText || arguments[0].textContent || '').trim();",
                element
        );
        return jsText == null ? "" : jsText.toString().trim();
    }
}
