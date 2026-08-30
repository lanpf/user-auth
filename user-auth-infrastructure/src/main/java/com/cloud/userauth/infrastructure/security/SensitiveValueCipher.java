package com.cloud.userauth.infrastructure.security;

import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Reversible AES-256-GCM encryption for sensitive values written to logs or other sinks;
 * ciphertext is prefixed with a version tag so the key can be rotated.
 */
public final class SensitiveValueCipher {

    private static final String PREFIX = "enc:v1:";
    private static final int IV_BYTES = 12;
    private static final int KEY_BYTES = 32;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public SensitiveValueCipher(String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        Assert.isTrue(decoded.length == KEY_BYTES, "log cipher key must decode to 32 bytes");
        this.key = new SecretKeySpec(decoded, "AES");
    }

    public String encrypt(String value) {
        Assert.hasText(value, "value must not be empty");
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(IV_BYTES + encrypted.length).put(iv).put(encrypted).array());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("log cipher encryption failed", exception);
        }
    }

    public String decrypt(String token) {
        Assert.isTrue(token != null && token.startsWith(PREFIX), "token must be an enc:v1 value");
        try {
            byte[] data = Base64.getDecoder().decode(token.substring(PREFIX.length()));
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, data, 0, IV_BYTES));
            byte[] plain = cipher.doFinal(data, IV_BYTES, data.length - IV_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("log cipher decryption failed", exception);
        }
    }
}
