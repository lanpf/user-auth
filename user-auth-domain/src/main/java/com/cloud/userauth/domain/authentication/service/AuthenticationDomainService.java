package com.cloud.userauth.domain.authentication.service;

import com.cloud.framework.domain.DomainEventIdGenerator;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.CredentialStatus;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.event.AuthAccountCreatedEvent;
import com.cloud.userauth.domain.authentication.event.UserLoggedInEvent;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthenticationDomainService {
    private final DomainEventIdGenerator domainEventIdGenerator;

    public AuthAccountCreationEffect createAuthAccountWithMobile(
            AuthAccountId authAccountId,
            UserId userId,
            CredentialId credentialId,
            LoginMobile mobile,
            Instant createdAt
    ) {
        AuthAccount authAccount = AuthAccount.createWithMobile(
                authAccountId, userId, credentialId, mobile, createdAt);
        return new AuthAccountCreationEffect(
                authAccount,
                List.of(new AuthAccountCreatedEvent(
                        domainEventIdGenerator.nextId(), createdAt, authAccount.id(), authAccount.userId()
                ))
        );
    }

    public LoginAuthenticationEffect login(
            AuthAccount authAccount,
            CredentialId authenticatedCredentialId,
            SessionId sessionId,
            LoginScene loginScene,
            Device device,
            Client client,
            Instant loggedInAt,
            Instant sessionExpiresAt
    ) {
        authAccount.ensureCanLogin();
        boolean credentialActive = authAccount.credentials().stream()
                .anyMatch(credential -> credential.id().equals(authenticatedCredentialId)
                        && credential.status() == CredentialStatus.ACTIVE);
        if (!credentialActive) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_NOT_FOUND);
        }
        LoginSession loginSession = LoginSession.create(
                sessionId,
                authAccount.userId(),
                authAccount.id(),
                authenticatedCredentialId,
                loginScene,
                device,
                client,
                loggedInAt,
                sessionExpiresAt
        );
        return new LoginAuthenticationEffect(
                loginSession,
                List.of(new UserLoggedInEvent(
                        domainEventIdGenerator.nextId(), loggedInAt, authAccount.userId(), authAccount.id(), loginSession.id()
                ))
        );
    }
}
