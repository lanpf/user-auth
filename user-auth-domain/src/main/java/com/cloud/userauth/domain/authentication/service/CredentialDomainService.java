package com.cloud.userauth.domain.authentication.service;

import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.Credential;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.event.CredentialDisabledEvent;
import com.cloud.userauth.domain.authentication.event.ExternalCredentialBoundEvent;
import com.cloud.userauth.domain.authentication.event.LoginMobileChangedEvent;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CredentialDomainService {
    private final AuthAccountRepository authAccountRepository;

    public CredentialChangeEffect bindExternalCredential(
            AuthAccount authAccount,
            CredentialId credentialId,
            CredentialIssuer issuer,
            Principal principal,
            Instant boundAt
    ) {
        CredentialKey key = CredentialKey.external(issuer, principal);
        authAccountRepository.findByCredential(key)
                .filter(existing -> !existing.id().equals(authAccount.id()))
                .ifPresent(existing -> {
                    throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_ALREADY_EXISTS);
                });
        Credential credential = authAccount.bindExternalCredential(
                credentialId, issuer, principal, boundAt);
        return new CredentialChangeEffect(
                authAccount,
                credential,
                List.of(new ExternalCredentialBoundEvent(
                        boundAt, authAccount.id(), issuer, principal
                ))
        );
    }

    public CredentialChangeEffect replaceMobileCredential(
            AuthAccount authAccount,
            CredentialId newCredentialId,
            LoginMobile oldMobile,
            LoginMobile newMobile,
            Instant changedAt
    ) {
        CredentialKey newMobileKey = CredentialKey.mobile(newMobile);
        authAccountRepository.findByCredential(newMobileKey)
                .filter(existing -> !existing.id().equals(authAccount.id()))
                .ifPresent(existing -> {
                    throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_ALREADY_EXISTS);
                });
        Credential credential = authAccount.replaceMobileCredential(
                newCredentialId, newMobile, changedAt);
        return new CredentialChangeEffect(
                authAccount,
                credential,
                List.of(new LoginMobileChangedEvent(
                        changedAt,
                        authAccount.userId(), authAccount.id(), oldMobile, newMobile
                ))
        );
    }

    public CredentialChangeEffect disableCredential(
            AuthAccount authAccount,
            CredentialKey key,
            Instant disabledAt
    ) {
        authAccount.disableCredential(key, disabledAt);
        return new CredentialChangeEffect(
                authAccount,
                null,
                List.of(new CredentialDisabledEvent(disabledAt, key))
        );
    }

}
