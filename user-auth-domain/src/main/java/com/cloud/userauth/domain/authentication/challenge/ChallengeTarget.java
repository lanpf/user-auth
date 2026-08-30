package com.cloud.userauth.domain.authentication.challenge;

import com.cloud.framework.core.validation.Require;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.common.DomainException;

public record ChallengeTarget(String value) {
    public ChallengeTarget {
        value = Require.notBlank(value, DomainException::missingField).trim();
    }

    public static ChallengeTarget of(AuthChallengeType type, String rawTarget) {
        return switch (type) {
            case SMS_OTP -> new ChallengeTarget(new LoginMobile(rawTarget).value());
        };
    }
}
