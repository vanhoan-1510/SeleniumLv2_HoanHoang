package com.hoanhoang.core.element;

public interface Element {
    void click();
    void type(String text);
    void shouldHaveText(String expected);
    void shouldHaveValue(String expected);
    void shouldHave(String prop, String expected);
}