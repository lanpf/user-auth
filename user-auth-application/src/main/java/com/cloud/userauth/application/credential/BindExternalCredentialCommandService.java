package com.cloud.userauth.application.credential;

import com.cloud.framework.domain.DomainEventStore;
import com.cloud.userauth.application.port.ExternalIdentityVerifierRegistry;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.external.ProofParameters;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authentication.credential.CredentialIdGenerator;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.ProofType;
import com.cloud.userauth.domain.authentication.service.CredentialChangeEffect;
import com.cloud.userauth.domain.authentication.service.CredentialDomainService;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.user.UserId;
import jakarta.validation.Valid;
import java.time.Clock;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class BindExternalCredentialCommandService {
    private final ExternalIdentityVerifierRegistry verifierRegistry;
    private final AuthAccountRepository authAccountRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final CredentialIdGenerator credentialIdGenerator;
    private final CredentialDomainService credentialDomainService;
    private final DomainEventStore domainEventStore;
    private final Clock clock;

    @Transactional
    public void execute(@Valid BindExternalCredentialCommand command) {
        LoginSession loginSession = loginSessionRepository.findById(
                        new SessionId(command.authenticatedSessionId()))
                .orElseThrow(() -> new DomainException(DomainError.LOGIN_SESSION_NOT_FOUND));
        loginSession.ensureActive(clock.instant());
        if (!loginSession.getUserId().equals(new UserId(command.authenticatedUserId()))) {
            throw new DomainException(DomainError.LOGIN_SESSION_NOT_FOUND);
        }
        AuthAccount account = authAccountRepository.findById(
                        loginSession.getAuthAccountId())
                .orElseThrow(() -> new DomainException(DomainError.AUTH_ACCOUNT_NOT_FOUND));
        if (!account.userId().equals(new UserId(command.authenticatedUserId()))) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_NOT_FOUND);
        }
        account.ensureCanLogin();
        ExternalIdentity identity = verifierRegistry.verify(
                command.issuer(), ProofType.AUTHORIZATION_CODE,
                Map.of(ProofParameters.AUTHORIZATION_CODE, command.authorizationCode()));
        CredentialKey key = CredentialKey.external(identity.issuer(), identity.principal());
        if (account.hasActiveCredential(key)) {
            return;
        }
        CredentialChangeEffect effect = credentialDomainService.bindExternalCredential(
                account, credentialIdGenerator.nextId(), identity.issuer(), identity.principal(),
                clock.instant());
        authAccountRepository.save(effect.authAccount());
        domainEventStore.appendAll(effect.events());
    }
}
