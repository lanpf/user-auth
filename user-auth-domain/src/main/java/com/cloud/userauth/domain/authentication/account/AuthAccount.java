package com.cloud.userauth.domain.authentication.account;

import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.Credential;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

@Getter
public class AuthAccount implements AggregateRoot<AuthAccountId> {
    private final AuthAccountId id;
    private final UserId userId;
    private AuthAccountStatus status;
    private final List<Credential> credentials;
    private final Instant createdAt;
    private Instant updatedAt;

    private AuthAccount(
            AuthAccountId id,
            UserId userId,
            AuthAccountStatus status,
            List<Credential> credentials,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.credentials = new ArrayList<>(credentials);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AuthAccount createWithMobile(
            AuthAccountId id,
            UserId userId,
            CredentialId credentialId,
            LoginMobile mobile,
            Instant createdAt
    ) {
        Credential mobileCredential = Credential.mobile(credentialId, mobile, createdAt);
        return new AuthAccount(
                id,
                userId,
                AuthAccountStatus.ACTIVE,
                Collections.singletonList(mobileCredential),
                createdAt,
                createdAt
        );
    }

    public static AuthAccount restore(
            AuthAccountId id,
            UserId userId,
            AuthAccountStatus status,
            List<Credential> credentials,
            Instant createdAt,
            Instant updatedAt
    ) {
        AuthAccount account = new AuthAccount(id, userId, status, credentials, createdAt, updatedAt);
        if (status != AuthAccountStatus.DISABLED) {
            account.ensureExactlyOneActiveMobileCredential();
        }
        return account;
    }

    public List<Credential> credentials() {
        return Collections.unmodifiableList(credentials);
    }

    @Override
    public AuthAccountId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public void ensureCanLogin() {
        if (status == AuthAccountStatus.DISABLED) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_DISABLED);
        }
        if (status == AuthAccountStatus.LOCKED) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_LOCKED);
        }
        if (!hasActiveMobileCredential()) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED);
        }
    }

    public boolean hasActiveMobileCredential() {
        return credentials.stream().anyMatch(Credential::isActiveMobile);
    }

    public Optional<Credential> activeMobileCredential() {
        return credentials.stream().filter(Credential::isActiveMobile).findFirst();
    }

    public boolean hasActiveCredential(CredentialKey key) {
        return credentials.stream()
                .anyMatch(credential -> credential.key().equals(key)
                        && credential.status() == com.cloud.userauth.domain.authentication.credential.CredentialStatus.ACTIVE);
    }

    public Credential bindExternalCredential(
            CredentialId credentialId,
            CredentialIssuer issuer,
            Principal externalPrincipal,
            Instant boundAt
    ) {
        ensureCanLogin();
        CredentialKey key = CredentialKey.external(issuer, externalPrincipal);
        if (hasActiveCredential(key)) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_ALREADY_EXISTS);
        }
        Credential credential = Credential.external(credentialId, issuer, externalPrincipal, boundAt);
        credentials.add(credential);
        updatedAt = boundAt;
        return credential;
    }

    public Credential replaceMobileCredential(
            CredentialId newCredentialId,
            LoginMobile newMobile,
            Instant changedAt
    ) {
        Credential current = activeMobileCredential()
                .orElseThrow(() -> new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED));
        if (current.key().equals(CredentialKey.mobile(newMobile))) {
            return current;
        }
        current.revokeForMobileReplacement(changedAt);
        Credential credential = Credential.mobile(newCredentialId, newMobile, changedAt);
        credentials.add(credential);
        updatedAt = changedAt;
        ensureExactlyOneActiveMobileCredential();
        return credential;
    }

    public void disableCredential(CredentialKey key, Instant disabledAt) {
        Credential credential = credentials.stream()
                .filter(item -> item.key().equals(key))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_NOT_FOUND));
        if (credential.getCredentialType() == com.cloud.userauth.domain.authentication.credential.CredentialType.MOBILE) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED);
        }
        credential.disable(disabledAt);
        updatedAt = disabledAt;
    }

    public void disable(Instant disabledAt) {
        this.status = AuthAccountStatus.DISABLED;
        this.updatedAt = disabledAt;
    }

    public void lock(Instant lockedAt) {
        this.status = AuthAccountStatus.LOCKED;
        this.updatedAt = lockedAt;
    }

    public void activate(Instant activatedAt) {
        this.status = AuthAccountStatus.ACTIVE;
        this.updatedAt = activatedAt;
    }

    private long activeMobileCredentialCount() {
        return credentials.stream().filter(Credential::isActiveMobile).count();
    }

    private void ensureExactlyOneActiveMobileCredential() {
        if (activeMobileCredentialCount() != 1) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED);
        }
    }
}
