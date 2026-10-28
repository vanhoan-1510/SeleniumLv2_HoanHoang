package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.UiWait;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class HeaderComponent extends BasePage {

    private final By allDepartments = By.xpath("//span[normalize-space()='All departments']");
    private final By categoryElectronic =
            By.xpath(
                    "//ul[@id='menu-all-departments-1']//a[contains(@class,'item-link')][normalize-space()='Electronic Components & Supplies']");

    @Step("Open login page")
    public void goToLoginPage() {
        goToPath("my-account");
    }

    @Step("Open Shop page")
    public void goToShopPage() {
        goToPath("shop");
    }

    @Step("Hover All departments and open 'Electronic Components & Supplies'")
    public void goToCategoryElectronicPage() {
        $(allDepartments).hover();
        $(categoryElectronic).jsClick();
        // Wait for the category page itself: the previous page's product cards would match too.
        UiWait.until(
                "Electronic Components & Supplies category page opened",
                d -> d.getCurrentUrl().contains(ElectronicPage.PATH));
        ApplicationWait.idle();
    }

    @Step("Open Shopping Cart page")
    public void goToCartPage() {
        UiWait.click(By.xpath("//a[contains(@href,'/cart') and not(contains(@href,'remove'))]"));
        new ShoppingCartPage().waitUntilLoaded();
    }

    @Step("Open My Account page")
    public void goToMyAccountPage() {
        goToPath("my-account");
    }

    private void goToPath(String path) {
        String baseUrl = ConfigLoader.getConfig().getBaseUrl().replaceAll("/+$", "");
        DriverManager.getDriver().get(baseUrl + "/" + path + "/");
        ApplicationWait.idle();
    }
}
