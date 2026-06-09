package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;

public class HeaderComponent extends BasePage {

    By login = By.xpath("//a[contains(@href,'account')]");
    By shopLink = By.xpath("//a[contains(@href,'shop')]");
    By allDepartments = By.xpath("//span[normalize-space()='All departments']");
    By categoryElectronic = By.xpath("//ul[@id='menu-all-departments-1']//a[contains(@class,'item-link')][normalize-space()='Electronic Components & Supplies']");
    By cartLink = By.xpath("//a[contains(@href,'cart')]");
    By myAccountLink = By.xpath("//a[contains(@href,'my-account')]");

    public void goToLoginPage() {
        goToPath("my-account");
    }

    public void goToShopPage() {
        goToPath("shop");
    }

    public void goToCategoryElectronicPage() {
        $(allDepartments).hover();
        $(categoryElectronic).jsClick();
    }

    public void goToCartPage() {
        goToPath("cart");
    }

    public void goToMyAccountPage() {
        goToPath("my-account");
    }

    private void goToPath(String path) {
        String baseUrl = ConfigLoader.getConfig().getBaseUrl().replaceAll("/+$", "");
        DriverManager.getDriver().get(baseUrl + "/" + path + "/");
    }
}
