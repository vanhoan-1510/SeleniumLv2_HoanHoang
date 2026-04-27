package com.hoanhoang.core.element;

public enum Condition {
    VISIBLE,
    INVISIBLE,
    ENABLED,
    DISABLED,
    CLICKABLE,
    PRESENT,
    TEXT(String.class),
    ATTRIBUTE(String.class);

    private final Class<?> paramType;
    private final String paramValue;

    Condition() {
        this.paramType = null;
        this.paramValue = null;
    }

    Condition(Class<?> paramType) {
        this.paramType = paramType;
        this.paramValue = null;
    }

    Condition(Class<?> paramType, String paramValue) {
        this.paramType = paramType;
        this.paramValue = paramValue;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public String getParamValue() {
        return paramValue;
    }

    public boolean hasParam() {
        return paramType != null;
    }
}
