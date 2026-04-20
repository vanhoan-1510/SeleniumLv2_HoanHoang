package com.hoanhoang.pages;

import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    By usernameFile = By.id("username");
    By passwordFile = By.id("password");
    By loginButton = By.name("login");

    public void login(String username, String password) {
        $(usernameFile).type(username);
        $(passwordFile).type(password);
        $(loginButton).click();
    }
}

