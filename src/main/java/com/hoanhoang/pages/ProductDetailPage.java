package com.hoanhoang.pages;

import com.hoanhoang.pages.components.ReviewComponent;

public class ProductDetailPage extends BasePage {
    private final ReviewComponent reviews = new ReviewComponent();

    public ReviewComponent reviews() {
        return reviews;
    }
}
