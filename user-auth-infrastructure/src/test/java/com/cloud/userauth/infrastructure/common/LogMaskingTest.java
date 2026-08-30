package com.cloud.userauth.infrastructure.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LogMaskingTest {

    @Test
    void shouldMaskMiddleDigitsOfMobileNumber() {
        assertEquals("138****1234", LogMasking.mobile("13800001234"));
    }

    @Test
    void shouldReturnPlaceholderForShortOrNullValues() {
        assertEquals("***", LogMasking.mobile("12345"));
        assertEquals("***", LogMasking.mobile(null));
    }
}
