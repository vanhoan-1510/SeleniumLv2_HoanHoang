package com.hoanhoang.core.element;

public interface Element {
    void click();
    void type(String text);
    void shouldHaveText(String expected);
    void shouldContainText(String expected);
    void shouldContainTextIgnoringCase(String expected);
    void shouldHaveValue(String expected);
    void shouldHave(String prop, String expected);

    String getText();

    String getValue();

    void hover();

    void jsClick();

    boolean isVisible();

    void shouldBeVisible();
}
