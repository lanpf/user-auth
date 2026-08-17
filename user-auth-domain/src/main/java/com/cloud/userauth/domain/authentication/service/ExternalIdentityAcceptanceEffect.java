package com.cloud.userauth.domain.authentication.service;

import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEffect;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import java.util.List;

public record ExternalIdentityAcceptanceEffect(
        ExternalIdentity externalIdentity,
        LoginMobile trustedMobile,
        LoginAttempt loginAttempt,
        List<DomainEvent> events
) implements DomainEffect {
    public ExternalIdentityAcceptanceEffect {
        events = List.copyOf(events);
    }

    public boolean isMobileTrusted() {
        return trustedMobile != null;
    }

    public boolean requiresMobileVerification() {
        return loginAttempt != null && loginAttempt.requiresMobileVerification();
    }
}
