package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import org.openqa.selenium.By;

public class BasePage {

    By login = By.xpath("//a[contains(@href,'account')]");
    By allDepartments = By.xpath("//span[normalize-space()='All departments']");
    By categoryElectronic = By.xpath("//ul[@id='menu-all-departments-1']//a[contains(@class,'item-link')][normalize-space()='Electronic Components & Supplies']");    By productCard = By.cssSelector(".woocommerce-Price-amount.amount");


    protected Element $(By locator) {
        return ElementFactory.$(locator);
    }

        public void goToLoginPage() {
            $(login).click();
        }

    public void goToCategoryElectronicPage() {
        $(allDepartments).hover();
        $(categoryElectronic).jsClick();
    }

        public void goToProductCardPage() {
            $(productCard).click();
        }
}