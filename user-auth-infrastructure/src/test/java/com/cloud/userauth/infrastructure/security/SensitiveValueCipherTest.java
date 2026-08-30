package com.cloud.userauth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class SensitiveValueCipherTest {
    private static final String KEY = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
    private static final SensitiveValueCipher CIPHER = new SensitiveValueCipher(KEY);

    @Test
    void shouldRoundTripEncryptedLogValue() {
        String token = CIPHER.encrypt("123456");

        assertTrue(token.startsWith("enc:v1:"));
        assertEquals("123456", CIPHER.decrypt(token));
    }

    @Test
    void shouldProduceDifferentCiphertextPerCallWithSamePlaintext() {
        String first = CIPHER.encrypt("123456");
        String second = CIPHER.encrypt("123456");

        assertNotEquals(first, second);
        assertEquals("123456", CIPHER.decrypt(first));
        assertEquals("123456", CIPHER.decrypt(second));
    }

    @Test
    void shouldNotContainPlaintextValue() {
        assertAll(
                () -> assertTrue(!CIPHER.encrypt("13800001234").contains("13800001234")),
                () -> assertTrue(!CIPHER.encrypt("654321").contains("654321")));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = CIPHER.encrypt("123456");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("aa") ? "bb" : "aa");

        assertThrows(IllegalStateException.class, () -> CIPHER.decrypt(tampered));
    }

    @Test
    void shouldRejectKeyThatDoesNotDecodeTo32Bytes() {
        assertThrows(IllegalArgumentException.class,
                () -> new SensitiveValueCipher(Base64.getEncoder().encodeToString(new byte[16])));
    }
}
