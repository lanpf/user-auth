package com.cloud.userauth.infrastructure.common;

/** Centralized log masking; masked output keeps the data type recognizable. */
public final class LogMasking {

    private LogMasking() {
    }

    public static String mobile(String value) {
        if (value == null || value.length() < 7) {
            return "***";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }
}
