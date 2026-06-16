package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopPage extends ProductListingPage {

    private static final String PRODUCT_CARD_XPATH = "(//div[(contains(@class,'products-grid') or contains(@class,'products-list'))]//div[contains(@class,'content-product')][.//a[contains(.,'Add to cart')]])[%s]";
    private final By addableProductCards = By.xpath("//div[(contains(@class,'products-grid') or contains(@class,'products-list'))]//div[contains(@class,'content-product')][.//a[contains(.,'Add to cart')]]");
    private final By productLinks = By.xpath("//div[contains(@class,'content-product')]//h2[contains(@class,'product-title')]//a");

    By gridViewButton = By.cssSelector("a[data-view='grid']");
    By listViewButton = By.cssSelector("a[data-type='list'], a[data-view='list']");
    By productGrid = By.xpath("//div[contains(@class,'products-grid')]");
    By productList = By.xpath("//div[contains(@class,'products-list')]");

    @Override
    protected String productCardXpath() {
        return PRODUCT_CARD_XPATH;
    }

    @Override
    protected By addableProductCards() {
        return addableProductCards;
    }

    public List<Map<String, String>> addMultipleRandomProductsToCart() {
        int numberOfProductsToAdd = new Random().nextInt(4) + 2;
        List<Map<String, String>> products = new ArrayList<>();

        for (int i = 0; i < numberOfProductsToAdd && getAddableProductCount() > 0; i++) {
            products.add(addRandomProductToCart());
        }

        return products;
    }

    public void sortByPriceLowToHigh() {
        selectSortOption("price");
    }

    public void sortByPriceHighToLow() {
        selectSortOption("price-desc");
    }

    public List<BigDecimal> getAllProductPrices() {
        List<BigDecimal> prices = new ArrayList<>();
        int count = getAddableProductCount();
        for (int i = 1; i <= count; i++) {
            String priceText = getProductPriceText(i);
            BigDecimal price = parsePrice(priceText);
            if (price.compareTo(BigDecimal.ZERO) > 0) {
                prices.add(price);
            }
        }
        return prices;
    }

    public boolean arePricesSortedLowToHigh(List<BigDecimal> prices) {
        List<BigDecimal> sortedPrices = new ArrayList<>(prices);
        Collections.sort(sortedPrices);
        return prices.equals(sortedPrices);
    }

    public boolean arePricesSortedHighToLow(List<BigDecimal> prices) {
        List<BigDecimal> sortedPrices = new ArrayList<>(prices);
        sortedPrices.sort(Comparator.reverseOrder());
        return prices.equals(sortedPrices);
    }

    private BigDecimal parsePrice(String priceText) {
        Matcher matcher = Pattern.compile("\\d+(?:[.,]\\d+)?").matcher(priceText);
        String currentPrice = null;
        while (matcher.find()) {
            currentPrice = matcher.group();
        }
        if (currentPrice == null) {
            throw new AssertionError("Could not parse product price from text: " + priceText);
        }
        return new BigDecimal(currentPrice.replace(",", "."));
    }

    private void selectSortOption(String value) {
        String baseUrl = ConfigLoader.getConfig().getBaseUrl().replaceAll("/+$", "");
        DriverManager.getDriver().get(baseUrl + "/shop/?orderby=" + value);
        getAddableProductCount();
    }

    public boolean isGridViewVisible() {
        return $(productGrid).isVisible();
    }

    public void verifyGridViewVisible() {
        $(productGrid).shouldBeVisible();
    }

    public boolean isListViewVisible() {
        return $(productList).isVisible();
    }

    public void verifyListViewVisible() {
        $(productList).shouldBeVisible();
    }

    public void verifyPricesSortedLowToHigh() {
        List<BigDecimal> prices = getAllProductPrices();
        if (!arePricesSortedLowToHigh(prices)) {
            List<BigDecimal> sortedPrices = new ArrayList<>(prices);
            Collections.sort(sortedPrices);
            throw new AssertionError("Prices are not sorted from low to high. Actual: " + prices + ", Expected: " + sortedPrices);
        }
    }

    public void verifyPricesSortedHighToLow() {
        List<BigDecimal> prices = getAllProductPrices();
        if (!arePricesSortedHighToLow(prices)) {
            List<BigDecimal> sortedPrices = new ArrayList<>(prices);
            sortedPrices.sort(Comparator.reverseOrder());
            throw new AssertionError("Prices are not sorted from high to low. Actual: " + prices + ", Expected: " + sortedPrices);
        }
    }

    public void switchToListView() {
        if (isGridViewVisible()) {
            clickVisibleViewButton(listViewButton);
        }
    }

    public void switchToGridView() {
        if (isListViewVisible()) {
            clickVisibleViewButton(gridViewButton);
        }
    }

    private void clickVisibleViewButton(By locator) {
        verifyWithRetry(() -> {
            WebElement button = DriverManager.getDriver().findElements(locator).stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No visible view button found: " + locator));
            ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].click();", button);
        });
    }

    public void clickFirstProduct() {
        getProductLinkCount();
        WebElement productLink = DriverManager.getDriver().findElements(productLinks).get(0);
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].scrollIntoView({block: 'center'});", productLink);
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("arguments[0].click();", productLink);
    }

    private int getProductLinkCount() {
        verifyWithRetry(() -> {
            int totalProducts = $$(productLinks).size();
            if (totalProducts == 0) {
                throw new AssertionError("No product links are visible on shop page");
            }
        });
        return $$(productLinks).size();
    }
}
