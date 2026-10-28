package com.hoanhoang.pages;

import com.hoanhoang.core.util.UiWait;
import io.qameta.allure.Allure;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    public void login(String username, String password) {
        // A programmatic step does not serialize the method's password argument.
        Allure.step(
                "Login as " + username,
                () -> {
                    $(By.id("username")).type(username);
                    $(By.id("password")).type(password);
                    $(By.name("login")).click();
                    UiWait.until(
                            "Account navigation displayed after login",
                            d ->
                                    UiWait.isVisible(
                                            By.cssSelector(".woocommerce-MyAccount-navigation")));
                });
    }
}
