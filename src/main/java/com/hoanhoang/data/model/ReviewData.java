package com.hoanhoang.data.model;

import java.util.Objects;

/** Review author data mapped by Gson from testdata/test-review.json. */
public record ReviewData(String name, String email) {
    public ReviewData {
        Objects.requireNonNull(name, "review name");
        Objects.requireNonNull(email, "review email");
    }
}
