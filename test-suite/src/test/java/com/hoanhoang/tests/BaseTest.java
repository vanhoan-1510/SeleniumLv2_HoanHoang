package com.hoanhoang.tests;

import com.hoanhoang.core.config.ConfigLoader;
import com.hoanhoang.core.driver.BrowserType;
import com.hoanhoang.core.driver.DriverFactory;
import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.report.listener.TestListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public class BaseTest {

    @BeforeMethod
    public void setUp() {
        String env = System.getProperty("env", "dev");
        ConfigLoader.load(env);
        String browser = ConfigLoader.getConfig().getBrowser();
        boolean headless = ConfigLoader.getConfig().isHeadless();
        BrowserType browserType = BrowserType.valueOf(browser.toUpperCase());
        DriverManager.setDriver(DriverFactory.createDriver(browserType, headless));
        DriverManager.getDriver().get(ConfigLoader.getConfig().getBaseUrl());
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.quitDriver();
    }
}