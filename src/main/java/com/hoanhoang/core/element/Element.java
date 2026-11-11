package com.hoanhoang.core.element;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import org.openqa.selenium.By;

public interface Element {
    // Actions
    void click();

    void jsClick();

    void type(String text);

    /** Removes focus through the browser's DOM API, without sending a navigation key. */
    void blur();

    /** Replaces the value with CTRL+A, then blurs once; does not call clear(). */
    void replaceTextAndBlur(String text);

    void selectByValue(String value);

    void hover();

    // State
    String getText();

    String getValue();

    String getDomAttribute(String name);

    String getDomProperty(String name);

    boolean exists();

    boolean isSelected();

    boolean isVisible();

    Element waitUntilVisible();

    Element waitUntilPresent();

    Element waitUntilSelected();

    /** Polls fresh read-only snapshots until the result is non-null and not Boolean.FALSE. */
    <T> T waitUntil(String description, Function<Element, T> condition);

    Element find(By locator);

    Elements findAll(By locator);

    /** Reads one DOM snapshot without nested waits. Callbacks must only read state. */
    <T> T readSnapshot(Function<Element, T> reader);

    /** Captures this node; the returned condition detects its removal, not a new locator match. */
    BooleanSupplier watchForReplacement();

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
}
