package com.cloud.userauth.domain.authentication.credential;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Instant;
import lombok.Getter;

@Getter
public class Credential {
    private final CredentialId credentialId;
    private final CredentialType credentialType;
    private final CredentialIssuer issuer;
    private final Principal principal;
    private CredentialStatus status;
    private Instant verifiedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Credential(
            CredentialId credentialId,
            CredentialType credentialType,
            CredentialIssuer issuer,
            Principal principal,
            CredentialStatus status,
            Instant verifiedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.credentialId = credentialId;
        this.credentialType = credentialType;
        this.issuer = issuer;
        this.principal = principal;
        this.status = status;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Credential mobile(
            CredentialId credentialId,
            LoginMobile mobile,
            Instant createdAt
    ) {
        return new Credential(
                credentialId,
                CredentialType.MOBILE,
                CredentialIssuer.LOCAL,
                Principal.mobile(mobile),
                CredentialStatus.ACTIVE,
                createdAt,
                createdAt,
                createdAt
        );
    }

    public static Credential external(
            CredentialId credentialId,
            CredentialIssuer issuer,
            Principal principal,
            Instant createdAt
    ) {
        if (issuer == null || issuer.isLocal()) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_IDENTITY_INVALID);
        }
        return new Credential(
                credentialId,
                CredentialType.EXTERNAL,
                issuer,
                principal,
                CredentialStatus.ACTIVE,
                createdAt,
                createdAt,
                createdAt
        );
    }

    public static Credential restore(
            CredentialId credentialId,
            CredentialType credentialType,
            CredentialIssuer issuer,
            Principal principal,
            CredentialStatus status,
            Instant verifiedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Credential(
                credentialId, credentialType, issuer, principal, status,
                verifiedAt, createdAt, updatedAt);
    }

    public CredentialKey key() {
        return new CredentialKey(credentialType, issuer, principal);
    }

    public CredentialId id() {
        return credentialId;
    }

    public CredentialStatus status() {
        return status;
    }

    public boolean isActiveMobile() {
        return credentialType == CredentialType.MOBILE && status == CredentialStatus.ACTIVE;
    }

    public boolean isActiveExternal(CredentialIssuer targetIssuer, Principal targetPrincipal) {
        return credentialType == CredentialType.EXTERNAL
                && status == CredentialStatus.ACTIVE
                && issuer.equals(targetIssuer)
                && principal.equals(targetPrincipal);
    }

    public void verify(Instant verifiedAt) {
        if (status == CredentialStatus.DISABLED) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_DISABLED);
        }
        this.status = CredentialStatus.ACTIVE;
        this.verifiedAt = verifiedAt;
        this.updatedAt = verifiedAt;
    }

    public void disable(Instant disabledAt) {
        if (credentialType == CredentialType.MOBILE) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED);
        }
        if (status == CredentialStatus.REVOKED) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_REVOKED);
        }
        this.status = CredentialStatus.DISABLED;
        this.updatedAt = disabledAt;
    }

    public void enable(Instant enabledAt) {
        if (credentialType == CredentialType.MOBILE || status == CredentialStatus.REVOKED) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_REVOKED);
        }
        this.status = CredentialStatus.ACTIVE;
        this.updatedAt = enabledAt;
    }

    public void revokeForMobileReplacement(Instant revokedAt) {
        if (credentialType != CredentialType.MOBILE || status != CredentialStatus.ACTIVE) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED);
        }
        this.status = CredentialStatus.REVOKED;
        this.updatedAt = revokedAt;
    }
}
