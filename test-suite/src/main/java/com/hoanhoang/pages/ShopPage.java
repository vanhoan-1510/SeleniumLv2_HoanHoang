package com.hoanhoang.pages;

import com.hoanhoang.core.element.BaseElement;
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

public class ShopPage extends BasePage {

    public String productNameXpath = "(//div[contains(@class,'content-product')])[%s]//h2[contains(@class,'product-title')]";
    public String productPriceXpath = "(//div[contains(@class,'content-product')])[%s]//span[contains(@class,'price')]";
    public String addToCardButtonXpath = "(//a[contains(.,'Add to cart')])[%s]";

    By sortInput = By.name("orderby");
    By sortByPriceLowToHigh = By.cssSelector("option[value='price']");
    By sortByPriceHighToLow = By.cssSelector("option[value='price-desc']");
    By gridViewButton = By.cssSelector("a[data-view='grid']");
    By listViewButton = By.cssSelector("a[data-type='list']");
    By productGrid = By.xpath("//div[contains(@class,'products-grid')]");
    By productList = By.xpath("//div[contains(@class,'products-list')]");

    public List<Map<String, String>> addMultipleRandomProductsToCart() {
        String containerXpath = "//div[contains(@class,'content-product')]";
        int totalProducts = BaseElement.count(containerXpath);

        int numberOfProductsToAdd = new Random().nextInt(4) + 2;

        Set<Integer> selectedIndexes = new HashSet<>();
        Random random = new Random();
        while (selectedIndexes.size() < numberOfProductsToAdd && selectedIndexes.size() < totalProducts) {
            selectedIndexes.add(random.nextInt(totalProducts) + 1);
        }

        List<Map<String, String>> products = new ArrayList<>();

        for (int index : selectedIndexes) {
            String name = new BaseElement(By.xpath(String.format(productNameXpath, index))).getText();
            String price = new BaseElement(By.xpath(String.format(productPriceXpath, index))).getText();

            new BaseElement(By.xpath(String.format(addToCardButtonXpath, index))).jsClick();

            Map<String, String> product = new HashMap<>();
            product.put("name", name);
            product.put("price", price);
            products.add(product);
        }

        return products;
    }

    public void sortByPriceLowToHigh() {
        $(sortByPriceLowToHigh).click();
    }

    public void sortByPriceHighToLow() {
        $(sortByPriceHighToLow).click();
    }

    public List<BigDecimal> getAllProductPrices() {
        List<BigDecimal> prices = new ArrayList<>();
        int count = BaseElement.count("//div[contains(@class,'content-product')]");
        for (int i = 1; i <= count; i++) {
            String xpath = String.format(productPriceXpath, i);
            String priceText = new BaseElement(By.xpath(xpath)).getText();
            BigDecimal price = parsePrice(priceText);
            prices.add(price);
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
        String cleaned = priceText.replaceAll("[^0-9.,]", "").replace(",", ".");
        return new BigDecimal(cleaned);
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
            throw new AssertionError("Prices are not sorted from low to high");
        }
    }

    public void verifyPricesSortedHighToLow() {
        List<BigDecimal> prices = getAllProductPrices();
        if (!arePricesSortedHighToLow(prices)) {
            throw new AssertionError("Prices are not sorted from high to low");
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
        new BaseElement(By.xpath(String.format(addToCardButtonXpath, 1))).jsClick();
    }

    public void clickFirstProduct() {
        By firstProductLink = By.xpath(String.format("(//div[contains(@class,'content-product')])[1]//h2[contains(@class,'product-title')]//a"));
        new BaseElement(firstProductLink).click();
    }
}
