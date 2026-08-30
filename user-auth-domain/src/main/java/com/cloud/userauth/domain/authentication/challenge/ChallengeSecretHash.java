package com.cloud.userauth.domain.authentication.challenge;

import com.cloud.framework.core.validation.Require;
import com.cloud.userauth.domain.common.DomainException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public record ChallengeSecretHash(String value) {
    public ChallengeSecretHash {
        value = Require.notBlank(value, DomainException::missingField).trim();
    }

    public boolean matches(ChallengeSecretHash other) {
        return other != null && MessageDigest.isEqual(
                value.getBytes(StandardCharsets.UTF_8),
                other.value.getBytes(StandardCharsets.UTF_8));
    }
}
