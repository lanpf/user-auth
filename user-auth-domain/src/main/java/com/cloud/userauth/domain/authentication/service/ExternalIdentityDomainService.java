package com.cloud.userauth.domain.authentication.service;

import com.cloud.framework.domain.DomainEvent;
import com.cloud.framework.domain.DomainEventIdGenerator;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.event.LoginAttemptCompletedEvent;
import com.cloud.userauth.domain.authentication.event.LoginAttemptCreatedEvent;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.IssuerMobileTrustPolicy;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ExternalIdentityDomainService {
    private final DomainEventIdGenerator domainEventIdGenerator;

    public ExternalIdentityAcceptanceEffect acceptExternalIdentityBinding(
            ExternalIdentity identity,
            LoginAttemptId loginAttemptId,
            Instant acceptedAt,
            Instant loginAttemptExpiresAt
    ) {
        LoginAttempt loginAttempt = LoginAttempt.createReady(
                loginAttemptId, identity, null, acceptedAt, loginAttemptExpiresAt);
        return acceptanceEffect(identity, null, loginAttempt, acceptedAt);
    }

    public ExternalIdentityAcceptanceEffect acceptExternalIdentity(
            ExternalIdentity identity,
            IssuerMobileTrustPolicy trustPolicy,
            LoginAttemptId loginAttemptId,
            Instant acceptedAt,
            Instant loginAttemptExpiresAt
    ) {
        if (trustPolicy != null && trustPolicy.canTrustMobile(identity)) {
            LoginAttempt loginAttempt = LoginAttempt.createReady(
                    loginAttemptId, identity, identity.mobile(), acceptedAt, loginAttemptExpiresAt);
            return acceptanceEffect(identity, identity.mobile(), loginAttempt, acceptedAt);
        }
        LoginAttempt loginAttempt = LoginAttempt.createPending(
                loginAttemptId, identity, acceptedAt, loginAttemptExpiresAt);
        return acceptanceEffect(identity, null, loginAttempt, acceptedAt);
    }

    public List<DomainEvent> completeLoginAttempt(
            LoginAttempt loginAttempt,
            SessionId sessionId,
            Instant completedAt
    ) {
        loginAttempt.complete(sessionId, completedAt);
        return List.of(new LoginAttemptCompletedEvent(
                domainEventIdGenerator.nextId(), completedAt, loginAttempt.id()
        ));
    }

    private ExternalIdentityAcceptanceEffect acceptanceEffect(
            ExternalIdentity identity,
            LoginMobile trustedMobile,
            LoginAttempt loginAttempt,
            Instant acceptedAt
    ) {
        return new ExternalIdentityAcceptanceEffect(
                identity,
                trustedMobile,
                loginAttempt,
                List.of(new LoginAttemptCreatedEvent(
                        domainEventIdGenerator.nextId(),
                        acceptedAt,
                        loginAttempt.id(),
                        loginAttempt.getIssuer(),
                        loginAttempt.getPrincipal()
                )));
    }
}
