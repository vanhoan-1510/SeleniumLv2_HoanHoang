package com.hoanhoang.core.element;

import com.hoanhoang.core.driver.DriverManager;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BaseElements implements Elements {

    private final By locator;

    public BaseElements(By locator) {
        this.locator = locator;
    }

    private static org.openqa.selenium.WebDriver driver() {
        return DriverManager.getDriver();
    }

    private List<org.openqa.selenium.WebElement> getElements() {
        return driver().findElements(locator);
    }

    @Override
    public int size() {
        return getElements().size();
    }

    @Override
    public List<String> getTexts() {
        return getElements().stream()
                .map(org.openqa.selenium.WebElement::getText)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Elements filter(Predicate<Element> predicate) {
        List<Element> filtered = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            Element element = get(i);
            if (predicate.test(element)) {
                filtered.add(element);
            }
        }
        // Return a new Elements wrapper with filtered elements
        // For simplicity, we'll create a wrapper that only exposes the filtered elements
        return new FilteredElements(filtered);
    }

    @Override
    public Element get(int index) {
        List<org.openqa.selenium.WebElement> elements = getElements();
        if (index < 0 || index >= elements.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + elements.size());
        }
        return ElementFactory.$(elements.get(index));
    }

    @Override
    public Element first() {
        if (isEmpty()) {
            throw new IllegalStateException("Elements collection is empty");
        }
        return get(0);
    }

    @Override
    public Element last() {
        if (isEmpty()) {
            throw new IllegalStateException("Elements collection is empty");
        }
        return get(size() - 1);
    }

    private static class FilteredElements implements Elements {
        private final List<Element> elements;

        FilteredElements(List<Element> elements) {
            this.elements = elements;
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public List<String> getTexts() {
            return elements.stream()
                    .map(Element::getText)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }

        @Override
        public Elements filter(Predicate<Element> predicate) {
            List<Element> filtered = elements.stream()
                    .filter(predicate)
                    .collect(Collectors.toList());
            return new FilteredElements(filtered);
        }

        @Override
        public Element get(int index) {
            return elements.get(index);
        }

        @Override
        public Element first() {
            if (isEmpty()) {
                throw new IllegalStateException("Elements collection is empty");
            }
            return elements.get(0);
        }

        @Override
        public Element last() {
            if (isEmpty()) {
                throw new IllegalStateException("Elements collection is empty");
            }
            return elements.get(elements.size() - 1);
        }
    }
}
