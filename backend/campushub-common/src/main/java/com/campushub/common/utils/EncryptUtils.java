package com.campushub.common.utils;

public final class EncryptUtils {

    private EncryptUtils() {
    }

    public static String mask(String value) {
        return value == null ? null : "***";
    }
}