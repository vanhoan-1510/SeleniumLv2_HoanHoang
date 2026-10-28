package com.hoanhoang.core.element;

import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.openqa.selenium.WebElement;

/**
 * What an element should have: text, exact text, value or DOM property. Used by
 * Element.shouldHave(...).
 */
public final class Condition {
    private final String description;
    private final Function<WebElement, String> reader;
    private final BiPredicate<String, String> matcher;
    private final String expected;

    private Condition(
            String description,
            Function<WebElement, String> reader,
            BiPredicate<String, String> matcher,
            String expected) {
        this.description = description;
        this.reader = reader;
        this.matcher = matcher;
        this.expected = expected;
    }

    /** Visible text contains the expected text, ignoring case (like Selenide's text). */
    public static Condition text(String expected) {
        return new Condition(
                "text containing",
                e -> e.getText().trim(),
                (actual, exp) ->
                        actual.toLowerCase(Locale.ROOT).contains(exp.toLowerCase(Locale.ROOT)),
                expected);
    }

    /** Visible text equals the expected text exactly. */
    public static Condition exactText(String expected) {
        return new Condition("exact text", e -> e.getText().trim(), String::equals, expected);
    }

    /** Input value (DOM property "value") equals the expected value. */
    public static Condition value(String expected) {
        return new Condition("value", e -> e.getDomProperty("value"), String::equals, expected);
    }

    /** DOM property (e.g. "checked", "className") equals the expected value. */
    public static Condition prop(String name, String expected) {
        return new Condition(
                "property '" + name + "'", e -> e.getDomProperty(name), String::equals, expected);
    }

    String read(WebElement element) {
        return reader.apply(element);
    }

    boolean matches(String actual) {
        return actual != null && matcher.test(actual, expected);
    }

    @Override
    public String toString() {
        return description + " '" + expected + "'";
    }
}
