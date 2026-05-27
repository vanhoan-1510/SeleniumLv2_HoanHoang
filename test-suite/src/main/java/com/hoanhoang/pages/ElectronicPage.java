package com.hoanhoang.pages;

import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ElectronicPage extends BasePage {

    public String productNameXpath = "(//div[contains(@class,'content-product')])[%s]//h2[contains(@class,'product-title')]";
    public String productPriceXpath = "(//div[contains(@class,'content-product')])[%s]//span[contains(@class,'price')]";
    public String addToCardButtonXpath = "(//a[contains(.,'Add to cart')])[%s]";

    public Map<String, String> addRandomProductToCart() {
        int total = $$(By.xpath("//div[contains(@class,'content-product')]")).size();
        int index = new Random().nextInt(total) + 1;

        String name = $(By.xpath(String.format(productNameXpath, index))).getText();
        String price = $(By.xpath(String.format(productPriceXpath, index))).getText();
        String buttonXpath = String.format(addToCardButtonXpath, index);

        $(By.xpath(buttonXpath)).jsClick();

        Map<String, String> product = new HashMap<>();
        product.put("name", name);
        product.put("price", price);
        return product;
    }

}