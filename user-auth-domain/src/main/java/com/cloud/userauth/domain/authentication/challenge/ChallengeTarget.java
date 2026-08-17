package com.cloud.userauth.domain.authentication.challenge;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;

public record ChallengeTarget(String value) {
    public ChallengeTarget {
        if (value == null || value.isBlank()) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        value = value.trim();
    }

    public static ChallengeTarget of(AuthChallengeType type, String rawTarget) {
        if (type == null) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        return switch (type) {
            case SMS_OTP -> new ChallengeTarget(new LoginMobile(rawTarget).value());
        };
    }
}
