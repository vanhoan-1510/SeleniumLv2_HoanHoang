package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class OrderHistoryPage extends BasePage {

    private static final String ORDER_TABLE_CSS = "table[class*='account-orders-table']";
    private static final String ORDER_ROW_TEMPLATE = "tbody tr:nth-child(%d)";
    private static final String ORDER_NUMBER_TEMPLATE = ORDER_ROW_TEMPLATE + " td[data-title='Order'] a";
    private static final String ORDER_DATE_TEMPLATE = ORDER_ROW_TEMPLATE + " td[data-title='Date'] time";
    private static final String ORDER_TOTAL_TEMPLATE = ORDER_ROW_TEMPLATE + " td[data-title='Total'] > span";
    private static final String ORDER_STATUS_TEMPLATE = ORDER_ROW_TEMPLATE + " td[data-title='Status']";

    public boolean isOrderTableVisible() {
        return $(By.cssSelector(ORDER_TABLE_CSS)).isVisible();
    }

    public void verifyOrderTableVisible() {
        $(By.cssSelector(ORDER_TABLE_CSS)).shouldBeVisible();
    }

    public void verifyMinimumOrderCount(int minCount) {
        int actualCount = getOrderCount();
        if (actualCount < minCount) {
            throw new AssertionError("Expected at least " + minCount + " orders, but found: " + actualCount);
        }
    }

    public int getOrderCount() {
        return $$(By.xpath("//tr[parent::tbody]")).size();
    }

    public boolean isOrderRowVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_ROW_TEMPLATE, index));
        return $(locator).isVisible();
    }

    public void verifyOrderRowVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_ROW_TEMPLATE, index));
        $(locator).shouldBeVisible();
    }

    public String getOrderNumber(int index) {
        By locator = By.cssSelector(String.format(ORDER_NUMBER_TEMPLATE, index));
        return $(locator).getText();
    }

    public String getOrderDate(int index) {
        By locator = By.cssSelector(String.format(ORDER_DATE_TEMPLATE, index));
        return $(locator).getText();
    }

    public String getOrderTotal(int index) {
        By locator = By.cssSelector(String.format(ORDER_TOTAL_TEMPLATE, index));
        return $(locator).getText();
    }

    public String getOrderStatus(int index) {
        By locator = By.cssSelector(String.format(ORDER_STATUS_TEMPLATE, index));
        return $(locator).getText();
    }

    public void verifyOrderDetailsDisplayed(int index) {
        verifyOrderRowVisible(index);
        verifyOrderNumberVisible(index);
        verifyOrderDateVisible(index);
        verifyOrderTotalVisible(index);
        verifyOrderStatusVisible(index);
    }

    public boolean isOrderNumberVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_NUMBER_TEMPLATE, index));
        return $(locator).isVisible();
    }

    public void verifyOrderNumberVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_NUMBER_TEMPLATE, index));
        $(locator).shouldBeVisible();
    }

    public boolean isOrderDateVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_DATE_TEMPLATE, index));
        return $(locator).isVisible();
    }

    public void verifyOrderDateVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_DATE_TEMPLATE, index));
        $(locator).shouldBeVisible();
    }

    public boolean isOrderTotalVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_TOTAL_TEMPLATE, index));
        return $(locator).isVisible();
    }

    public void verifyOrderTotalVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_TOTAL_TEMPLATE, index));
        $(locator).shouldBeVisible();
    }

    public boolean isOrderStatusVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_STATUS_TEMPLATE, index));
        return $(locator).isVisible();
    }

    public void verifyOrderStatusVisible(int index) {
        By locator = By.cssSelector(String.format(ORDER_STATUS_TEMPLATE, index));
        $(locator).shouldBeVisible();
    }
}