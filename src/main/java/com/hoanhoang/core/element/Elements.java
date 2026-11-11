package com.hoanhoang.core.element;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Elements {

    int size();

    boolean isEmpty();

    Elements filter(Predicate<Element> predicate);

    default Elements visible() {
        return filter(Element::isVisible);
    }

    /** Reads the current nodes together; snapshot getters and child lookups never wait. */
    <T> T readSnapshot(Function<List<Element>, T> reader);

    /** Polls a fresh read-only node list on every attempt; callbacks must not start waits. */
    <T> T waitUntil(String description, Function<List<Element>, T> condition);

    Elements waitUntilNotEmpty();

    Elements waitUntilSizeLessThan(int count);

    default <T> List<T> map(Function<Element, T> mapper) {
        return readSnapshot(elements -> elements.stream().map(mapper).toList());
    }

    Element get(int index);

    Element first();

    Element last();
}
