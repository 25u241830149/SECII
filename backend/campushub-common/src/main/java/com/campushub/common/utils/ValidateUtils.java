package com.campushub.common.utils;

public final class ValidateUtils {

    private ValidateUtils() {
    }

    public static boolean isPositive(Long value) {
        return value != null && value > 0;
    }
}