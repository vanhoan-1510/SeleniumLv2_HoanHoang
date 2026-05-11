package com.hoanhoang.tests;

import com.hoanhoang.pages.MyAccountPage;
import com.hoanhoang.pages.OrderHistoryPage;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TC05 extends BaseTest {

    @BeforeMethod
    @Override
    public void setUp(ITestContext context) {
        super.setUp(context);

        createOrder();
        createOrder();
    }

    @Test
    public void tc5_verifyOrdersInOrderHistory() {
        MyAccountPage myAccountPage = new MyAccountPage();
        OrderHistoryPage orderHistoryPage = new OrderHistoryPage();

        myAccountPage.goToMyAccountPage();
        myAccountPage.clickOnOrders();

        orderHistoryPage.verifyMinimumOrderCount(2);
        orderHistoryPage.verifyOrderTableVisible();

        int orderCount = orderHistoryPage.getOrderCount();
        int ordersToCheck = Math.min(orderCount, 2);

        for (int i = 1; i <= ordersToCheck; i++) {
            orderHistoryPage.verifyOrderDetailsDisplayed(i);

            String orderNumber = orderHistoryPage.getOrderNumber(i);
            String orderDate = orderHistoryPage.getOrderDate(i);
            String orderTotal = orderHistoryPage.getOrderTotal(i);
            String orderStatus = orderHistoryPage.getOrderStatus(i);

            System.out.println("Order " + i + " Number: " + orderNumber);
            System.out.println("Order " + i + " Date: " + orderDate);
            System.out.println("Order " + i + " Total: " + orderTotal);
            System.out.println("Order " + i + " Status: " + orderStatus);
        }
    }
}
