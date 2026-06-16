package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class ElectronicPage extends ProductListingPage {

    private static final String PRODUCT_CARD_XPATH = "(//div[contains(@class,'content-product')][.//a[contains(.,'Add to cart')]])[%s]";
    private final By addableProductCards = By.xpath("//div[contains(@class,'content-product')][.//a[contains(.,'Add to cart')]]");

    @Override
    protected String productCardXpath() {
        return PRODUCT_CARD_XPATH;
    }

    @Override
    protected By addableProductCards() {
        return addableProductCards;
    }
}
