package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import org.openqa.selenium.By;

public class OrderSuccessPage extends BasePage {

    By statusMessage = By.cssSelector(".woocommerce-notice.woocommerce-thankyou-order-received");
    By orderOverviewSection = By.cssSelector("ul.woocommerce-order-overview.order_details");
    By orderNumber = By.cssSelector("li.woocommerce-order-overview__order > strong");
    By orderDate = By.cssSelector("li.woocommerce-order-overview__date > strong");
    By orderEmail = By.cssSelector("li.woocommerce-order-overview__email > strong");
    By orderTotal = By.cssSelector("li.woocommerce-order-overview__total > strong .woocommerce-Price-amount");
    By orderPaymentMethod = By.cssSelector("li.woocommerce-order-overview__payment-method > strong");

    By orderDetailsSection = By.cssSelector("section.woocommerce-order-details");
    By orderDetailsTable = By.cssSelector("table.woocommerce-table--order-details");
    By orderProductName = By.cssSelector("td.woocommerce-table__product-name.product-name > a");
    By orderProductQty = By.cssSelector("td.woocommerce-table__product-name .product-quantity");
    By orderProductPrice = By.cssSelector("td.woocommerce-table__product-total .woocommerce-Price-amount");
    By orderSubtotal = By.xpath("//tfoot/tr[th[text()='Subtotal:']]/td//span[@class='woocommerce-Price-amount amount']");
    By orderTotalFooter = By.xpath("//tfoot/tr[th[text()='Total:']]/td//span[@class='woocommerce-Price-amount amount']");

    By billingSection = By.cssSelector("section.woocommerce-customer-details");
    By billingPhone = By.cssSelector("p.woocommerce-customer-details--phone");
    By billingEmail = By.cssSelector("p.woocommerce-customer-details--email");

    By billingName = By.cssSelector(".woocommerce-column--billing-address address");

    protected Element $(By locator) {
        return ElementFactory.$(locator);
    }

    public boolean isOrderSuccessHeadingVisible() {
        return $(statusMessage).isVisible();
    }

    public void verifyOrderSuccessHeadingVisible() {
        $(statusMessage).shouldBeVisible();
    }

    public String getOrderedProductName() {
        return $(orderProductName).getText();
    }

    public String getOrderedProductPrice() {
        return $(orderProductPrice).getText();
    }

    public String getBillingName() {
        return $(billingName).getText();
    }

    public String getStatusMessage() {
        return $(statusMessage).getText();
    }

    public String getOrderNumber() {
        return $(orderNumber).getText();
    }

    public String getOrderDate() {
        return $(orderDate).getText();
    }

    public String getOrderTotal() {
        return $(orderTotal).getText();
    }

    public String getPaymentMethod() {
        return $(orderPaymentMethod).getText();
    }

    public String getOrderProductQty() {
        return $(orderProductQty).getText();
    }

    public String getBillingPhone() {
        return $(billingPhone).getText();
    }

    public String getBillingEmail() {
        return $(billingEmail).getText();
    }
}