package com.cloud.userauth.application.login;

import com.cloud.framework.domain.DomainEventStore;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ChallengeSecretHasher;
import com.cloud.userauth.application.registration.RegistrationProcess;
import com.cloud.userauth.application.registration.RegistrationProcessId;
import com.cloud.userauth.application.registration.RegistrationProcessRepository;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeRepository;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeConsumerType;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.CredentialIdGenerator;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.service.AuthAccountCreationEffect;
import com.cloud.userauth.domain.authentication.service.AuthenticationDomainService;
import com.cloud.userauth.domain.authentication.service.LoginAuthenticationEffect;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.domain.user.UserIdGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** 手机号登录流程中的本地事务命令服务。 */
@RequiredArgsConstructor
public class MobileOtpLoginTransactionService {
    private final AuthChallengeRepository challengeRepository;
    private final RegistrationProcessRepository registrationRepository;
    private final AuthAccountRepository authAccountRepository;
    private final LoginSessionRepository sessionRepository;
    private final UserIdGenerator userIdGenerator;
    private final CredentialIdGenerator credentialIdGenerator;
    private final ChallengeSecretHasher secretHasher;
    private final AuthenticationDomainService authenticationDomainService;
    private final DomainEventStore domainEventStore;
    private final Clock clock;
    private final Duration sessionTtl;

    @Transactional
    public AuthChallenge verifyChallenge(MobileAuthenticationCommand command) {
        AuthChallenge challenge = requiredChallenge(new AuthChallengeId(command.challengeId()));
        challenge.verify(
                AuthChallengeType.SMS_OTP,
                AuthChallengeScene.LOGIN,
                secretHasher.hash(challenge.id(), command.code()),
                clock.instant());
        challengeRepository.save(challenge);
        return challenge;
    }

    @Transactional
    public RegistrationProcess startRegistration(AuthChallengeId challengeId, LoginMobile mobile) {
        RegistrationProcess existing = registrationRepository.findByChallengeId(challengeId).orElse(null);
        if (existing != null) {
            return existing;
        }
        AuthChallenge challenge = requiredChallenge(challengeId);
        UserId userId = userIdGenerator.nextId();
        AuthAccountCreationEffect effect = authenticationDomainService.createAuthAccountWithMobile(
                authAccountRepository.nextId(), userId, credentialIdGenerator.nextId(), mobile, clock.instant());
        AuthAccount account = effect.authAccount();
        RegistrationProcess process = RegistrationProcess.accountCreated(
                registrationRepository.nextId(), challenge.id(), userId, account.id(), clock.instant());
        authAccountRepository.save(account);
        registrationRepository.save(process);
        challenge.consume(
                ChallengeConsumerType.REGISTRATION_PROCESS,
                String.valueOf(process.id().value()),
                clock.instant());
        challengeRepository.save(challenge);
        domainEventStore.appendAll(effect.events());
        return process;
    }

    @Transactional
    public void markUserInitialized(RegistrationProcessId processId) {
        RegistrationProcess process = requiredProcess(processId);
        process.markUserInitialized(clock.instant());
        registrationRepository.save(process);
    }

    @Transactional
    public void markRetryableFailure(RegistrationProcessId processId, String failure) {
        RegistrationProcess process = requiredProcess(processId);
        process.markRetryableFailure(failure, clock.instant());
        registrationRepository.save(process);
    }

    @Transactional
    public MobileAuthenticationCommandOutput completeRegistration(
            RegistrationProcessId processId,
            MobileAuthenticationCommand command
    ) {
        RegistrationProcess process = requiredProcess(processId);
        if (process.isCompleted()) {
            return replayLogin(process.getSessionId(), true);
        }
        AuthAccount account = requiredAccount(process.getAuthAccountId());
        LoginSession session = createLogin(account, command);
        process.complete(session.id(), clock.instant());
        registrationRepository.save(process);
        return output(account, session, true, false);
    }

    @Transactional
    public MobileAuthenticationCommandOutput loginExistingAccount(
            AuthAccountId accountId,
            AuthChallengeId challengeId,
            MobileAuthenticationCommand command
    ) {
        AuthAccount account = requiredAccount(accountId);
        AuthChallenge challenge = requiredChallenge(challengeId);
        LoginSession session = createLogin(account, command);
        challenge.consume(
                ChallengeConsumerType.LOGIN_SESSION,
                session.id().value(),
                clock.instant());
        challengeRepository.save(challenge);
        return output(account, session, false, false);
    }

    @Transactional(readOnly = true)
    public MobileAuthenticationCommandOutput replayLogin(SessionId sessionId, boolean fromRegistrationFlow) {
        LoginSession session = requiredSession(sessionId);
        session.ensureActive(clock.instant());
        return new MobileAuthenticationCommandOutput(
                session.getUserId().value(), session.getAuthAccountId().value(), session.id().value(),
                fromRegistrationFlow, true
        );
    }

    private RegistrationProcess requiredProcess(RegistrationProcessId processId) {
        return registrationRepository.findById(processId)
                .orElseThrow(() -> new ApplicationException(ApplicationError.APP_REGISTRATION_PROCESS_NOT_FOUND));
    }

    private LoginSession requiredSession(SessionId sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new DomainException(DomainError.LOGIN_SESSION_NOT_FOUND));
    }

    private AuthAccount requiredAccount(AuthAccountId accountId) {
        return authAccountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException(DomainError.AUTH_ACCOUNT_NOT_FOUND));
    }

    private AuthChallenge requiredChallenge(AuthChallengeId challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new DomainException(DomainError.AUTH_CHALLENGE_NOT_FOUND));
    }

    private LoginSession createLogin(
            AuthAccount account,
            MobileAuthenticationCommand command
    ) {
        Instant now = clock.instant();
        CredentialId authenticatedCredentialId = account.activeMobileCredential()
                .orElseThrow(() -> new DomainException(DomainError.AUTH_ACCOUNT_MOBILE_CREDENTIAL_REQUIRED))
                .id();
        LoginAuthenticationEffect effect = authenticationDomainService.login(
                account,
                authenticatedCredentialId,
                sessionRepository.nextId(),
                LoginScene.MOBILE_LOGIN,
                new Device(command.deviceId(), command.deviceType(), command.deviceName()),
                new Client(command.clientAppId(), command.clientPlatform(), command.clientVersion()),
                now,
                now.plus(sessionTtl));
        sessionRepository.save(effect.session());
        domainEventStore.appendAll(effect.events());
        return effect.session();
    }

    private MobileAuthenticationCommandOutput output(
            AuthAccount account,
            LoginSession session,
            boolean fromRegistrationFlow,
            boolean replayed
    ) {
        return new MobileAuthenticationCommandOutput(
                account.userId().value(), account.id().value(), session.id().value(),
                fromRegistrationFlow, replayed);
    }
}
