package com.hoanhoang.pages;

import com.hoanhoang.core.element.Element;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Allure;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    private final Element usernameInput = $(By.id("username"));
    private final Element passwordInput = $(By.id("password"));
    private final Element loginButton = $(By.name("login"));
    private final Element accountNavigation =
            $(By.cssSelector(".woocommerce-MyAccount-navigation"));

    public void login(String username, String password) {
        // A programmatic step does not serialize the method's password argument.
        Allure.step(
                "Login as " + username,
                () -> {
                    usernameInput.type(username);
                    passwordInput.type(password);
                    loginButton.click();
                    accountNavigation.waitUntil(
                            "Account navigation displayed after login",
                            navigation -> {
                                ApplicationWait.failIfCriticalError();
                                return navigation.isVisible();
                            });
                });
    }
}
