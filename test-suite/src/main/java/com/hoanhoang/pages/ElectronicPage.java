package com.hoanhoang.pages;

import com.hoanhoang.core.element.BaseElement;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ElectronicPage extends BasePage {

    public String productNameXpath = "(//div[contains(@class,'content-product')])[%s]//h2[contains(@class,'product-title')]";
    public String productPriceXpath = "(//div[contains(@class,'content-product')])[%s]//span[contains(@class,'price')]";
    public String addToCardButtonXpath = "(//a[contains(.,'Add to cart')])[%s]";

    public Map<String, String> addRandomProductToCart() {
        String containerXpath = "//div[contains(@class,'content-product')]";
        int total = BaseElement.count(containerXpath);
        int index = new Random().nextInt(total) + 1;

        String name = new BaseElement(By.xpath(String.format(productNameXpath, index))).getText();
        String price = new BaseElement(By.xpath(String.format(productPriceXpath, index))).getText();

        new BaseElement(By.xpath(String.format(addToCardButtonXpath, index))).jsClick();

        Map<String, String> product = new HashMap<>();
        product.put("name", name);
        product.put("price", price);
        return product;
    }

}
