package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class MyAccountPage extends BasePage {

    By ordersLink = By.xpath("//a[normalize-space()='Orders']");
    By dashboardHeading = By.cssSelector("h1[class='title'] span");


    public void clickOnOrders() {
        $(ordersLink).click();
    }

    public boolean isDashboardVisible() {
        return $(dashboardHeading).isVisible();
    }
}
