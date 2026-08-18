package com.cloud.userauth.application.login.external;

import com.cloud.framework.domain.DomainEventStore;
import com.cloud.userauth.application.port.ChallengeSecretHasher;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeRepository;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeConsumerType;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.Credential;
import com.cloud.userauth.domain.authentication.credential.CredentialIdGenerator;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttempt;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptId;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptRepository;
import com.cloud.userauth.domain.authentication.loginattempt.LoginAttemptStatus;
import com.cloud.userauth.domain.authentication.service.AuthAccountCreationEffect;
import com.cloud.userauth.domain.authentication.service.AuthenticationDomainService;
import com.cloud.userauth.domain.authentication.service.CredentialChangeEffect;
import com.cloud.userauth.domain.authentication.service.CredentialDomainService;
import com.cloud.userauth.domain.authentication.service.ExternalIdentityDomainService;
import com.cloud.userauth.domain.authentication.service.LoginAuthenticationEffect;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.domain.user.UserIdGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ExternalLoginTransactionService {
    private final LoginAttemptRepository loginAttemptRepository;
    private final AuthChallengeRepository authChallengeRepository;
    private final AuthAccountRepository authAccountRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final UserIdGenerator userIdGenerator;
    private final CredentialIdGenerator credentialIdGenerator;
    private final ChallengeSecretHasher secretHasher;
    private final AuthenticationDomainService authenticationDomainService;
    private final CredentialDomainService credentialDomainService;
    private final ExternalIdentityDomainService externalIdentityDomainService;
    private final DomainEventStore domainEventStore;
    private final Clock clock;
    private final Duration sessionTtl;

    @Transactional
    public ExternalAccountPreparationOutput prepareAccount(ExternalAuthenticationCommand command) {
        LoginAttempt loginAttempt = requiredLoginAttempt(command.loginAttemptId());
        Instant now = clock.instant();
        if (loginAttempt.getStatus() == LoginAttemptStatus.COMPLETED) {
            LoginSession session = requiredSession(loginAttempt.getSessionId());
            AuthAccount account = requiredAccount(session.getAuthAccountId());
            return new ExternalAccountPreparationOutput(
                    account.userId(), account.id(), session.getAuthenticatedCredentialId());
        }
        loginAttempt.ensureUsable(now);
        if (loginAttempt.requiresMobileVerification()) {
            verifyMobile(loginAttempt, command, now);
        }

        if (!command.bindExternalIdentity()) {
            return prepareTrustedMobileAccount(loginAttempt, now);
        }

        CredentialKey externalKey = CredentialKey.external(
                loginAttempt.getIssuer(), loginAttempt.getExternalPrincipal());
        AuthAccount externalAccount = authAccountRepository.findByCredential(externalKey).orElse(null);
        if (externalAccount != null) {
            ensureMobileAccountDoesNotConflict(loginAttempt, externalAccount);
            Credential externalCredential = activeCredential(externalAccount, externalKey);
            return new ExternalAccountPreparationOutput(
                    externalAccount.userId(), externalAccount.id(), externalCredential.id());
        }

        LoginMobile mobile = loginAttempt.getMobile();
        if (mobile == null || !loginAttempt.isMobileVerified()) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_MOBILE_UNTRUSTED);
        }
        AuthAccount account = authAccountRepository
                .findByCredential(CredentialKey.mobile(mobile))
                .orElseGet(() -> createAccount(mobile, now));
        CredentialChangeEffect binding = credentialDomainService.bindExternalCredential(
                account,
                credentialIdGenerator.nextId(),
                loginAttempt.getIssuer(),
                loginAttempt.getExternalPrincipal(),
                now);
        authAccountRepository.save(binding.authAccount());
        domainEventStore.appendAll(binding.events());
        return new ExternalAccountPreparationOutput(
                account.userId(), account.id(), binding.credential().id());
    }

    @Transactional
    public ExternalAuthenticationOutput completeLogin(
            ExternalAuthenticationCommand command,
            ExternalAccountPreparationOutput prepared
    ) {
        LoginAttempt loginAttempt = requiredLoginAttempt(command.loginAttemptId());
        if (loginAttempt.getStatus() == LoginAttemptStatus.COMPLETED) {
            LoginSession replayed = requiredSession(loginAttempt.getSessionId());
            replayed.ensureActive(clock.instant());
            return output(replayed, true);
        }
        Instant now = clock.instant();
        loginAttempt.ensureReady(now);
        AuthAccount account = requiredAccount(prepared.authAccountId());
        LoginAuthenticationEffect login = authenticationDomainService.login(
                account,
                prepared.externalCredentialId(),
                loginSessionRepository.nextId(),
                LoginScene.EXTERNAL_LOGIN,
                new Device(command.deviceId(), command.deviceType(), command.deviceName()),
                new Client(command.clientAppId(), command.clientPlatform(), command.clientVersion()),
                now,
                now.plus(sessionTtl));
        LoginSession session = login.session();
        loginSessionRepository.save(session);
        loginAttemptRepository.save(complete(loginAttempt, session, now));
        domainEventStore.appendAll(login.events());
        return output(session, false);
    }

    private void verifyMobile(
            LoginAttempt loginAttempt,
            ExternalAuthenticationCommand command,
            Instant verifiedAt
    ) {
        if (command.challengeId() == null || !StringUtils.hasText(command.code())) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_MOBILE_UNTRUSTED);
        }
        AuthChallenge challenge = authChallengeRepository
                .findById(new AuthChallengeId(command.challengeId()))
                .orElseThrow(() -> new DomainException(DomainError.AUTH_CHALLENGE_NOT_FOUND));
        challenge.verify(
                AuthChallengeType.SMS_OTP,
                AuthChallengeScene.COMPLETE_EXTERNAL_LOGIN,
                secretHasher.hash(challenge.id(), command.code()),
                verifiedAt);
        loginAttempt.verifyMobile(new LoginMobile(challenge.getTarget().value()), verifiedAt);
        challenge.consume(
                ChallengeConsumerType.LOGIN_ATTEMPT,
                loginAttempt.id().value(),
                verifiedAt);
        authChallengeRepository.save(challenge);
        loginAttemptRepository.save(loginAttempt);
    }

    private AuthAccount createAccount(LoginMobile mobile, Instant createdAt) {
        UserId userId = userIdGenerator.nextId();
        AuthAccountCreationEffect effect = authenticationDomainService.createAuthAccountWithMobile(
                authAccountRepository.nextId(),
                userId,
                credentialIdGenerator.nextId(),
                mobile,
                createdAt);
        authAccountRepository.save(effect.authAccount());
        domainEventStore.appendAll(effect.events());
        return effect.authAccount();
    }

    private ExternalAccountPreparationOutput prepareTrustedMobileAccount(
            LoginAttempt loginAttempt,
            Instant now
    ) {
        LoginMobile mobile = loginAttempt.getMobile();
        if (mobile == null || !loginAttempt.isMobileVerified()) {
            throw new DomainException(DomainError.AUTH_ACCOUNT_EXTERNAL_MOBILE_UNTRUSTED);
        }
        AuthAccount account = authAccountRepository.findByCredential(CredentialKey.mobile(mobile))
                .orElseGet(() -> createAccount(mobile, now));
        Credential credential = activeCredential(account, CredentialKey.mobile(mobile));
        return new ExternalAccountPreparationOutput(account.userId(), account.id(), credential.id());
    }

    private void ensureMobileAccountDoesNotConflict(
            LoginAttempt loginAttempt,
            AuthAccount externalAccount
    ) {
        if (loginAttempt.getMobile() == null || !loginAttempt.isMobileVerified()) {
            return;
        }
        authAccountRepository.findByCredential(CredentialKey.mobile(loginAttempt.getMobile()))
                .filter(mobileAccount -> !mobileAccount.id().equals(externalAccount.id()))
                .ifPresent(mobileAccount -> {
                    throw new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_ALREADY_EXISTS);
                });
    }

    private LoginAttempt complete(
            LoginAttempt loginAttempt,
            LoginSession session,
            Instant completedAt
    ) {
        domainEventStore.appendAll(externalIdentityDomainService.completeLoginAttempt(
                loginAttempt, session.id(), completedAt));
        return loginAttempt;
    }

    private LoginAttempt requiredLoginAttempt(String id) {
        return loginAttemptRepository.findById(new LoginAttemptId(id))
                .orElseThrow(() -> new DomainException(DomainError.LOGIN_ATTEMPT_NOT_FOUND));
    }

    private AuthAccount requiredAccount(com.cloud.userauth.domain.authentication.account.AuthAccountId id) {
        return authAccountRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainError.AUTH_ACCOUNT_NOT_FOUND));
    }

    private LoginSession requiredSession(com.cloud.userauth.domain.authentication.session.SessionId id) {
        return loginSessionRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainError.LOGIN_SESSION_NOT_FOUND));
    }

    private static Credential activeCredential(AuthAccount account, CredentialKey key) {
        return account.credentials().stream()
                .filter(credential -> credential.key().equals(key)
                        && credential.status() == com.cloud.userauth.domain.authentication.credential.CredentialStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainError.AUTH_ACCOUNT_CREDENTIAL_NOT_FOUND));
    }

    private static ExternalAuthenticationOutput output(LoginSession session, boolean replayed) {
        return new ExternalAuthenticationOutput(
                session.getUserId().value(),
                session.getAuthAccountId().value(),
                session.id().value(),
                replayed);
    }
}
