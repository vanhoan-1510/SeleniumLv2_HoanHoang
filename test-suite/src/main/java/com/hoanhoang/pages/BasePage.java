package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.core.element.ElementFactory;
import org.openqa.selenium.By;

public class BasePage {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_INTERVAL_MS = 1000;

    By login = By.xpath("//a[contains(@href,'account')]");
    By shopLink = By.xpath("//a[contains(@href,'shop')]");
    By allDepartments = By.xpath("//span[normalize-space()='All departments']");
    By categoryElectronic = By.xpath("//ul[@id='menu-all-departments-1']//a[contains(@class,'item-link')][normalize-space()='Electronic Components & Supplies']");
    By cartLink = By.xpath("//a[contains(@href,'cart')]");
    By myAccountLink = By.xpath("//a[contains(@href,'my-account')]");

    protected Element $(By locator) {
        return ElementFactory.$(locator);
    }

    public void goToLoginPage() {
            $(login).click();
        }

    public void goToShopPage() {
        $(shopLink).click();
    }

    public void goToCategoryElectronicPage() {
        $(allDepartments).hover();
        $(categoryElectronic).jsClick();
    }

    protected Element find(By locator) {
        return ElementFactory.$(locator);
    }

    public void goToCartPage() {
        $(cartLink).click();
    }

    public void goToMyAccountPage() {
        $(myAccountLink).click();
    }

    protected void verifyWithRetry(Runnable verification) {
        AssertionError lastError = null;
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                verification.run();
                return;
            } catch (AssertionError e) {
                lastError = e;
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted during retry", ie);
                    }
                }
            }
        }
        throw lastError;
    }

    protected void assertEquals(String message, String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ". Expected: '" + expected + "', Actual: '" + actual + "'");
        }
    }

    protected void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    protected void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}