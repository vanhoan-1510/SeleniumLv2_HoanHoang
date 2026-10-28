package com.hoanhoang.core.element;

public interface Element {
    // Actions
    void click();

    void jsClick();

    void type(String text);

    void hover();

    // State
    String getText();

    String getValue();

    boolean isVisible();

    // Checks: wait (auto-retry) until the condition holds, otherwise fail with expected vs actual.
    Element shouldHave(Condition condition);

    default Element shouldHaveText(String text) {
        return shouldHave(Condition.text(text));
    }

    default Element shouldHaveExactText(String text) {
        return shouldHave(Condition.exactText(text));
    }

    default Element shouldHaveValue(String value) {
        return shouldHave(Condition.value(value));
    }

    default Element shouldHaveProp(String name, String value) {
        return shouldHave(Condition.prop(name, value));
    }
}
