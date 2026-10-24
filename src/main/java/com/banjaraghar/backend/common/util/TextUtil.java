package com.banjaraghar.backend.common.util;

public final class TextUtil {

    private TextUtil() {
    }

    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
