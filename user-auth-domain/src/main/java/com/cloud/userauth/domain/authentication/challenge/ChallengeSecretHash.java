package com.cloud.userauth.domain.authentication.challenge;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public record ChallengeSecretHash(String value) {
    public ChallengeSecretHash {
        if (value == null || value.isBlank()) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
    }

    public boolean matches(ChallengeSecretHash other) {
        return other != null && MessageDigest.isEqual(
                value.getBytes(StandardCharsets.UTF_8),
                other.value.getBytes(StandardCharsets.UTF_8));
    }
}
