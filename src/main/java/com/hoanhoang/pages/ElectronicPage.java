package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class ElectronicPage extends ProductListingPage {
    public static final String PATH = "/product-category/electronic-components-supplies/";
    public static final String TITLE =
            "Electronic Components & Supplies – TestArchitect Sample Website";

    @Override
    protected By addableProductCards() {
        return By.cssSelector(".content-product:has(a.add_to_cart_button)");
    }
}
