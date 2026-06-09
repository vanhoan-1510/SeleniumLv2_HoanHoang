package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopPage extends BasePage {

    public String productCardXpath = "(//div[contains(@class,'products-grid')]//div[contains(@class,'content-product')][.//a[contains(.,'Add to cart')]])[%s]";
    public String productNameXpath = productCardXpath + "//h2[contains(@class,'product-title')]";
    public String productPriceXpath = productCardXpath + "//span[contains(@class,'price')]";
    public String addToCardButtonXpath = productCardXpath + "//a[contains(.,'Add to cart')]";
    private final By addableProductCards = By.xpath("//div[contains(@class,'products-grid')]//div[contains(@class,'content-product')][.//a[contains(.,'Add to cart')]]");

    By gridViewButton = By.cssSelector("a[data-view='grid']");
    By listViewButton = By.cssSelector("a[data-type='list']");
    By productGrid = By.xpath("//div[contains(@class,'products-grid')]");
    By productList = By.xpath("//div[contains(@class,'products-list')]");

    public List<Map<String, String>> addMultipleRandomProductsToCart() {
        int totalProducts = getAddableProductCount();

        int numberOfProductsToAdd = new Random().nextInt(4) + 2;

        Set<Integer> selectedIndexes = new HashSet<>();
        Random random = new Random();
        while (selectedIndexes.size() < numberOfProductsToAdd && selectedIndexes.size() < totalProducts) {
            selectedIndexes.add(random.nextInt(totalProducts) + 1);
        }

        List<Map<String, String>> products = new ArrayList<>();

        for (int index : selectedIndexes) {
            String name = $(By.xpath(String.format(productNameXpath, index))).getText();
            String price = $(By.xpath(String.format(productPriceXpath, index))).getText();

            addProductToCart(index);

            Map<String, String> product = new HashMap<>();
            product.put("name", name);
            product.put("price", price);
            products.add(product);
        }

        return products;
    }

    private int getAddableProductCount() {
        verifyWithRetry(() -> {
            int totalProducts = $$(addableProductCards).size();
            if (totalProducts == 0) {
                throw new AssertionError("No addable products are visible on shop page");
            }
        });
        return $$(addableProductCards).size();
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
            String xpath = String.format(productPriceXpath, i);
            String priceText = $(By.xpath(xpath)).getText();
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
            $(listViewButton).click();
        }
    }

    public void switchToGridView() {
        if (isListViewVisible()) {
            $(gridViewButton).click();
        }
    }

    public void addFirstProductToCart() {
        addProductToCart(1);
    }

    private void addProductToCart(int index) {
        $(By.xpath(String.format(addToCardButtonXpath, index))).jsClick();
        waitForCartUpdate();
    }

    private void waitForCartUpdate() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for cart update", e);
        }
    }

    public void clickFirstProduct() {
        By firstProductLink = By.xpath(String.format("(//div[contains(@class,'content-product')])[1]//h2[contains(@class,'product-title')]//a"));
        $(firstProductLink).click();
    }
}
