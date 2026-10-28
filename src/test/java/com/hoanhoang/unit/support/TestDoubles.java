package com.hoanhoang.unit.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Small Selenium doubles shared by the browser-free regression checks. */
public final class TestDoubles {
    private TestDoubles() {}

    public static WebElement mockElement(String text) {
        return mockElement(
                (proxy, method, args) ->
                        switch (method.getName()) {
                            case "isDisplayed", "isEnabled" -> true;
                            case "getText" -> text;
                            default -> null;
                        });
    }

    public static WebDriver mockDriver(AtomicReference<List<WebElement>> nodes) {
        return mockDriver(
                (proxy, method, args) ->
                        switch (method.getName()) {
                            case "findElements" -> nodes.get();
                            default -> null;
                        },
                JavascriptExecutor.class);
    }

    public static WebElement mockElement(InvocationHandler handler) {
        return mock(WebElement.class, handler);
    }

    public static WebDriver mockDriver(
            InvocationHandler handler, Class<?>... additionalInterfaces) {
        return mock(WebDriver.class, handler, additionalInterfaces);
    }

    private static <T> T mock(
            Class<T> primaryInterface,
            InvocationHandler handler,
            Class<?>... additionalInterfaces) {
        Class<?>[] interfaces = new Class<?>[additionalInterfaces.length + 1];
        interfaces[0] = primaryInterface;
        System.arraycopy(additionalInterfaces, 0, interfaces, 1, additionalInterfaces.length);
        return primaryInterface.cast(
                Proxy.newProxyInstance(WebDriver.class.getClassLoader(), interfaces, handler));
    }
}
