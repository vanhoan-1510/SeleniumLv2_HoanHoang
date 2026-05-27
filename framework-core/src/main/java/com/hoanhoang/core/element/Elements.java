package com.hoanhoang.core.element;

import java.util.List;

public interface Elements {

    int size();

    List<String> getTexts();

    boolean isEmpty();

    Elements filter(java.util.function.Predicate<Element> predicate);

    Element get(int index);

    Element first();

    Element last();
}