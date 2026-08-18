package com.cloud.userauth.application.login.external;

import com.cloud.framework.domain.DomainEventStore;
import com.cloud.userauth.application.port.ExternalIdentityVerifierRegistry;
import com.cloud.userauth.application.port.IssuerMobileTrustPolicyProvider;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptRepository;
import com.cloud.userauth.domain.authentication.service.ExternalIdentityAcceptanceEffect;
import com.cloud.userauth.domain.authentication.service.ExternalIdentityDomainService;
import com.cloud.userauth.domain.authentication.external.IssuerMobileTrustPolicy;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class ExternalLoginAttemptCommandService {
    private final ExternalIdentityVerifierRegistry verifierRegistry;
    private final IssuerMobileTrustPolicyProvider trustPolicyProvider;
    private final AuthAccountRepository authAccountRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final ExternalIdentityDomainService externalIdentityDomainService;
    private final DomainEventStore domainEventStore;
    private final Clock clock;
    private final Duration loginAttemptTtl;

    @Transactional
    public ExternalLoginAttemptOutput execute(@Valid ExternalLoginAttemptCommand command) {
        ExternalIdentity identity = verifierRegistry.verify(
                command.issuer(), command.proofType(), command.proofParameters());
        return accept(identity, trustPolicyProvider.policyFor(identity.issuer()));
    }

    /** 仅供已完成入口验签与证明校验的受保护调用链使用。 */
    @Transactional
    public ExternalLoginAttemptOutput acceptTrustedIdentity(ExternalIdentity identity) {
        return accept(identity, new IssuerMobileTrustPolicy(identity.issuer(), true));
    }

    private ExternalLoginAttemptOutput accept(
            ExternalIdentity identity,
            IssuerMobileTrustPolicy trustPolicy
    ) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(loginAttemptTtl);
        AuthAccount boundAccount = authAccountRepository
                .findByCredential(CredentialKey.external(identity.issuer(), identity.externalPrincipal()))
                .orElse(null);
        ExternalIdentityAcceptanceEffect acceptanceEffect;
        if (boundAccount != null) {
            boundAccount.ensureCanLogin();
            acceptanceEffect = externalIdentityDomainService.acceptBoundExternalIdentity(
                    identity, loginAttemptRepository.nextId(), now, expiresAt);
        } else {
            acceptanceEffect = externalIdentityDomainService.acceptExternalIdentity(
                    identity,
                    trustPolicy,
                    loginAttemptRepository.nextId(),
                    now,
                    expiresAt);
        }
        LoginAttempt loginAttempt = acceptanceEffect.loginAttempt();
        loginAttemptRepository.save(loginAttempt);
        domainEventStore.appendAll(acceptanceEffect.events());
        return new ExternalLoginAttemptOutput(
                loginAttempt.id().value(),
                loginAttempt.requiresMobileVerification(),
                loginAttempt.getExpiresAt());
    }
}
