package com.hoanhoang.pages;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.pages.support.ApplicationWait;
import io.qameta.allure.Step;

public class HomePage extends BasePage {
    @Step("Open home page")
    public void open() {
        DriverManager.getDriver().get(ConfigLoader.getConfig().getBaseUrl());
        ApplicationWait.idle();
    }
}
