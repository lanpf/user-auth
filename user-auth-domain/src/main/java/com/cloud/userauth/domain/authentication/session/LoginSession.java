package com.cloud.userauth.domain.authentication.session;

import com.cloud.framework.core.validation.Require;
import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.user.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginSession implements AggregateRoot<SessionId> {
    private final SessionId id;
    private final UserId userId;
    private final AuthAccountId authAccountId;
    private final CredentialId authenticatedCredentialId;
    private SessionStatus status;
    private final LoginScene loginScene;
    private final Device device;
    private final Client client;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private Instant lastActiveAt;

    public static LoginSession create(
            SessionId id,
            UserId userId,
            AuthAccountId authAccountId,
            CredentialId authenticatedCredentialId,
            LoginScene loginScene,
            Device device,
            Client client,
            Instant issuedAt,
            Instant expiresAt
    ) {
        Require.notNull(id, DomainException::missingField);
        Require.notNull(userId, DomainException::missingField);
        Require.notNull(authAccountId, DomainException::missingField);
        Require.notNull(authenticatedCredentialId, DomainException::missingField);
        Require.notNull(loginScene, DomainException::missingField);
        Require.notNull(device, DomainException::missingField);
        Require.notNull(client, DomainException::missingField);
        Require.notNull(issuedAt, DomainException::missingField);
        Require.notNull(expiresAt, DomainException::missingField);
        if (!expiresAt.isAfter(issuedAt)) {
            throw new DomainException(DomainError.DOMAIN_OBJECT_STATE_INVALID);
        }
        return new LoginSession(
                id,
                userId,
                authAccountId,
                authenticatedCredentialId,
                SessionStatus.ACTIVE,
                loginScene,
                device,
                client,
                issuedAt,
                expiresAt,
                issuedAt
        );
    }

    public static LoginSession restore(
            SessionId id,
            UserId userId,
            AuthAccountId authAccountId,
            CredentialId authenticatedCredentialId,
            SessionStatus status,
            LoginScene loginScene,
            Device device,
            Client client,
            Instant issuedAt,
            Instant expiresAt,
            Instant lastActiveAt
    ) {
        return new LoginSession(
                id, userId, authAccountId, authenticatedCredentialId, status, loginScene, device, client,
                issuedAt, expiresAt, lastActiveAt);
    }

    public void ensureActive(Instant now) {
        if (status != SessionStatus.ACTIVE) {
            throw new DomainException(DomainError.LOGIN_SESSION_INACTIVE);
        }
        if (now.isAfter(expiresAt)) {
            expire(now);
            throw new DomainException(DomainError.LOGIN_SESSION_INACTIVE);
        }
    }

    public void touch(Instant now) {
        ensureActive(now);
        this.lastActiveAt = now;
    }

    public void revoke(Instant now) {
        this.status = SessionStatus.REVOKED;
        this.lastActiveAt = now;
    }

    public void expire(Instant now) {
        if (status == SessionStatus.ACTIVE) {
            this.status = SessionStatus.EXPIRED;
            this.lastActiveAt = now;
        }
    }
}
