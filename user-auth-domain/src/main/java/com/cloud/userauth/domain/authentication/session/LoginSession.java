package com.cloud.userauth.domain.authentication.session;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
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

    private LoginSession(
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
        this.id = id;
        this.userId = userId;
        this.authAccountId = authAccountId;
        this.authenticatedCredentialId = authenticatedCredentialId;
        this.status = status;
        this.loginScene = loginScene;
        this.device = device;
        this.client = client;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.lastActiveAt = lastActiveAt;
    }

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
        if (authenticatedCredentialId == null || loginScene == null
                || issuedAt == null || expiresAt == null || !expiresAt.isAfter(issuedAt)) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
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

    @Override
    public SessionId id() {
        return id;
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
