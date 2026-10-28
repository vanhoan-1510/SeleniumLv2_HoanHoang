package com.hoanhoang.core.element;

import com.hoanhoang.core.driver.DriverManager;
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

    public BaseElements(By locator) {
        Objects.requireNonNull(locator);
        this.resolver = d -> d.findElements(locator);
        this.description = locator.toString();
    }

    private BaseElements(Function<WebDriver, List<WebElement>> resolver, String description) {
        this.resolver = resolver;
        this.description = description;
    }

    private List<WebElement> getElements() {
        return resolver.apply(DriverManager.getDriver());
    }

    @Override
    public int size() {
        return getElements().size();
    }

    @Override
    public List<String> getTexts() {
        return getElements().stream().map(WebElement::getText).toList();
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
                                .filter(e -> predicate.test(new PredicateElement(e)))
                                .toList(),
                description + " filtered");
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
                description + "[" + index + "]");
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
                description + "[last]");
    }

    /** A predicate reads one snapshot; only the enclosing collection poll retries that read. */
    private static final class PredicateElement extends BaseElement {
        private final WebElement current;

        private PredicateElement(WebElement current) {
            super(current);
            this.current = current;
        }

        @Override
        public String getText() {
            return current.getText().trim();
        }

        @Override
        public String getValue() {
            return current.getDomProperty("value");
        }

        @Override
        public boolean isVisible() {
            return current.isDisplayed();
        }
    }
}
