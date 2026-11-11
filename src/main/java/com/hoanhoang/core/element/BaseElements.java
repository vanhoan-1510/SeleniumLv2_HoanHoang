package com.hoanhoang.core.element;

import com.hoanhoang.core.driver.DriverManager;
import com.hoanhoang.core.util.WebDriverWaitHelper;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** A live query. Selection and filtering are evaluated again whenever an element is resolved. */
public class BaseElements implements Elements {
    private final Function<WebDriver, List<WebElement>> resolver;
    private final String description;
    private final boolean snapshot;

    public BaseElements(By locator) {
        Objects.requireNonNull(locator);
        this.resolver = d -> d.findElements(locator);
        this.description = locator.toString();
        this.snapshot = false;
    }

    BaseElements(
            Function<WebDriver, List<WebElement>> resolver, String description, boolean snapshot) {
        this.resolver = resolver;
        this.description = description;
        this.snapshot = snapshot;
    }

    private List<WebElement> getElements() {
        return resolver.apply(DriverManager.getDriver());
    }

    @Override
    public int size() {
        return getElements().size();
    }

    @Override
    public boolean isEmpty() {
        return getElements().isEmpty();
    }

    @Override
    public Elements filter(Predicate<Element> predicate) {
        Objects.requireNonNull(predicate);
        return new BaseElements(
                d ->
                        resolver.apply(d).stream()
                                .filter(e -> predicate.test(BaseElement.snapshot(e, description)))
                                .toList(),
                description + " filtered",
                snapshot);
    }

    @Override
    public <T> T readSnapshot(Function<List<Element>, T> reader) {
        List<Element> elements =
                getElements().stream()
                        .map(element -> BaseElement.snapshot(element, description))
                        .toList();
        return reader.apply(elements);
    }

    @Override
    public <T> T waitUntil(String expectation, Function<List<Element>, T> condition) {
        if (snapshot) {
            throw new IllegalStateException(
                    "Snapshot callbacks support reads only: " + description);
        }
        Objects.requireNonNull(condition);
        return WebDriverWaitHelper.until(expectation, driver -> readSnapshot(condition));
    }

    @Override
    public Elements waitUntilNotEmpty() {
        waitUntil(description + " not empty", elements -> !elements.isEmpty());
        return this;
    }

    @Override
    public Elements waitUntilSizeLessThan(int count) {
        waitUntil(description + " size less than " + count, elements -> elements.size() < count);
        return this;
    }

    /** Selects the current index, not the identity of its original DOM node. */
    @Override
    public Element get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Negative index: " + index);
        }
        return new BaseElement(
                d -> {
                    List<WebElement> elements = resolver.apply(d);
                    return index < elements.size() ? elements.get(index) : null;
                },
                description + "[" + index + "]",
                snapshot);
    }

    @Override
    public Element first() {
        return get(0);
    }

    @Override
    public Element last() {
        return new BaseElement(
                d -> {
                    List<WebElement> elements = resolver.apply(d);
                    return elements.isEmpty() ? null : elements.get(elements.size() - 1);
                },
                description + "[last]",
                snapshot);
    }
}
