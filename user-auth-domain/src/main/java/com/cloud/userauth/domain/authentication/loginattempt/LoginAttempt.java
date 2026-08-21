package com.cloud.userauth.domain.authentication.loginattempt;

import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.session.SessionId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class LoginAttempt implements AggregateRoot<LoginAttemptId> {
    private final LoginAttemptId id;
    private final CredentialIssuer issuer;
    private final Principal principal;
    private LoginMobile mobile;
    private boolean mobileVerified;
    private LoginAttemptStatus status;
    private SessionId sessionId;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private LoginAttempt(
            LoginAttemptId id,
            CredentialIssuer issuer,
            Principal principal,
            LoginMobile mobile,
            boolean mobileVerified,
            LoginAttemptStatus status,
            SessionId sessionId,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.issuer = issuer;
        this.principal = principal;
        this.mobile = mobile;
        this.mobileVerified = mobileVerified;
        this.status = status;
        this.sessionId = sessionId;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LoginAttempt createPending(
            LoginAttemptId id,
            ExternalIdentity identity,
            Instant createdAt,
            Instant expiresAt
    ) {
        return create(id, identity, null, false, LoginAttemptStatus.PENDING, createdAt, expiresAt);
    }

    public static LoginAttempt createReady(
            LoginAttemptId id,
            ExternalIdentity identity,
            LoginMobile trustedMobile,
            Instant createdAt,
            Instant expiresAt
    ) {
        return create(id, identity, trustedMobile, trustedMobile != null, LoginAttemptStatus.READY, createdAt, expiresAt);
    }

    private static LoginAttempt create(
            LoginAttemptId id,
            ExternalIdentity identity,
            LoginMobile mobile,
            boolean mobileVerified,
            LoginAttemptStatus status,
            Instant createdAt,
            Instant expiresAt
    ) {
        if (id == null || identity == null || createdAt == null || expiresAt == null
                || !expiresAt.isAfter(createdAt)) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_IDENTITY_INVALID);
        }
        return new LoginAttempt(
                id,
                identity.issuer(),
                identity.principal(),
                mobile,
                mobileVerified,
                status,
                null,
                expiresAt,
                createdAt,
                createdAt
        );
    }

    public static LoginAttempt restore(
            LoginAttemptId id,
            CredentialIssuer issuer,
            Principal principal,
            LoginMobile mobile,
            boolean mobileVerified,
            LoginAttemptStatus status,
            SessionId sessionId,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new LoginAttempt(
                id, issuer, principal, mobile, mobileVerified,
                status, sessionId, expiresAt, createdAt, updatedAt);
    }

    public void ensureUsable(Instant now) {
        if (status == LoginAttemptStatus.COMPLETED) {
            throw new DomainException(DomainError.LOGIN_ATTEMPT_COMPLETED);
        }
        if (status == LoginAttemptStatus.EXPIRED || !now.isBefore(expiresAt)) {
            expire(now);
            throw new DomainException(DomainError.LOGIN_ATTEMPT_EXPIRED);
        }
    }

    public boolean requiresMobileVerification() {
        return status == LoginAttemptStatus.PENDING;
    }

    public void verifyMobile(LoginMobile verifiedMobile, Instant verifiedAt) {
        ensureUsable(verifiedAt);
        if (status != LoginAttemptStatus.PENDING || verifiedMobile == null) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_IDENTITY_INVALID);
        }
        this.mobile = verifiedMobile;
        this.mobileVerified = true;
        this.status = LoginAttemptStatus.READY;
        this.updatedAt = verifiedAt;
    }

    public void ensureReady(Instant now) {
        ensureUsable(now);
        if (status != LoginAttemptStatus.READY) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_MOBILE_UNTRUSTED);
        }
    }

    public void complete(SessionId completedSessionId, Instant completedAt) {
        ensureReady(completedAt);
        if (completedSessionId == null) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        this.status = LoginAttemptStatus.COMPLETED;
        this.sessionId = completedSessionId;
        this.updatedAt = completedAt;
    }

    public void expire(Instant expiredAt) {
        if (status == LoginAttemptStatus.PENDING || status == LoginAttemptStatus.READY) {
            this.status = LoginAttemptStatus.EXPIRED;
            this.updatedAt = expiredAt;
        }
    }
}
