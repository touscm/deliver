package com.touscm.deliver.base.utils;

public class StringUtils {

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isEmpty() || value.trim().isEmpty();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isEmpty() && !value.trim().isEmpty();
    }

}
