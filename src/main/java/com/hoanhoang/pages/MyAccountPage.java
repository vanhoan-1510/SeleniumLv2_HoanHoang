package com.hoanhoang.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;

public class MyAccountPage extends BasePage {

    By ordersLink = By.xpath("//a[normalize-space()='Orders']");

    @Step("Click 'Orders' in My Account navigation")
    public void clickOnOrders() {
        $(ordersLink).click();
    }
}
