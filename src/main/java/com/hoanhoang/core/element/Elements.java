package com.hoanhoang.core.element;

import java.util.List;
import java.util.function.Predicate;

public interface Elements {

    int size();

    List<String> getTexts();

    boolean isEmpty();

    Elements filter(Predicate<Element> predicate);

    Element get(int index);

    Element first();

    Element last();
}
