package com.cloud.userauth;

import com.cloud.userauth.application.port.AuthChallengeIssuePolicy;
import com.cloud.userauth.application.challenge.AuthChallengeIssueCommandService;
import com.cloud.userauth.application.challenge.IssueAuthChallengeCommand;
import com.cloud.userauth.application.challenge.IssueAuthChallengeOutput;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.login.MobileOtpAuthenticationCommand;
import com.cloud.userauth.application.login.MobileOtpAuthenticationOutput;
import com.cloud.userauth.application.login.MobileOtpAuthenticationProcess;
import com.cloud.userauth.application.login.MobileOtpLoginTransactionService;
import com.cloud.userauth.application.port.AuthChallengeIssueLock;
import com.cloud.userauth.application.port.MobileOtpLoginLock;
import com.cloud.userauth.application.port.UserGateway;
import com.cloud.userauth.application.registration.RegistrationProcess;
import com.cloud.userauth.application.registration.RegistrationProcessStatus;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeStatus;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeConsumerType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.service.AuthenticationDomainService;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.challenge.FixedCodeGenerator;
import com.cloud.userauth.infrastructure.challenge.PropertiesAuthChallengePolicyProvider;
import com.cloud.userauth.infrastructure.config.AuthChallengeProperties;
import com.cloud.userauth.infrastructure.config.NonProductionAuthChallengeProperties;
import com.cloud.userauth.infrastructure.security.HmacSha256ChallengeSecretHasher;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.Accounts;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.Challenges;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.Registrations;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.Sessions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobileRegistrationRetryIT {
    @Test
    void shouldUseRefreshedFixedOneTimeCodeWithoutRecreatingGenerator() {
        NonProductionAuthChallengeProperties properties = new NonProductionAuthChallengeProperties();
        properties.setFixedCode("123456");
        FixedCodeGenerator generator = new FixedCodeGenerator(properties.getFixedCode());

        assertEquals("123456", generator.generate());

        properties.setFixedCode("654321");

        assertEquals("654321", generator.generate());
        System.out.printf("fixed one-time code refreshed: code=%s%n", generator.generate());
    }

    @Test
    void shouldApplyRefreshedChallengePolicyOnlyToNewChallenges() {
        Challenges challenges = new Challenges();
        AuthChallengeProperties properties = new AuthChallengeProperties();
        AuthChallengeIssueCommandService service = new AuthChallengeIssueCommandService(
                challenges,
                () -> "123456",
                new HmacSha256ChallengeSecretHasher("test-pepper"),
                (type, target, secret) -> {
                },
                AuthChallengeIssueLock.direct(),
                new PropertiesAuthChallengePolicyProvider(properties),
                java.time.Clock.systemUTC());

        IssueAuthChallengeOutput first = service.execute(new IssueAuthChallengeCommand(
                AuthChallengeType.SMS_OTP, AuthChallengeScene.LOGIN, "13800138000"));
        AuthChallenge firstChallenge = challenges.findById(new AuthChallengeId(first.challengeId())).orElseThrow();

        properties.getIssuePolicy().setTtl(java.time.Duration.ofMinutes(3));
        properties.getIssuePolicy().setReuseWindow(java.time.Duration.ofSeconds(30));
        IssueAuthChallengeOutput second = service.execute(new IssueAuthChallengeCommand(
                AuthChallengeType.SMS_OTP, AuthChallengeScene.LOGIN, "13900139000"));
        AuthChallenge secondChallenge = challenges.findById(new AuthChallengeId(second.challengeId())).orElseThrow();

        assertEquals(java.time.Duration.ofMinutes(5), java.time.Duration.between(
                firstChallenge.getCreatedAt(), firstChallenge.getExpiresAt()));
        assertEquals(java.time.Duration.ofSeconds(60), java.time.Duration.between(
                firstChallenge.getCreatedAt(), firstChallenge.getReusableUntil()));
        assertEquals(java.time.Duration.ofMinutes(3), java.time.Duration.between(
                secondChallenge.getCreatedAt(), secondChallenge.getExpiresAt()));
        assertEquals(java.time.Duration.ofSeconds(30), java.time.Duration.between(
                secondChallenge.getCreatedAt(), secondChallenge.getReusableUntil()));
        System.out.printf(
                "challenge policy refreshed: firstTtl=%s, secondTtl=%s%n",
                java.time.Duration.between(firstChallenge.getCreatedAt(), firstChallenge.getExpiresAt()),
                java.time.Duration.between(secondChallenge.getCreatedAt(), secondChallenge.getExpiresAt()));
    }

    @Test
    void shouldResumeSameRegistrationProcessAfterUserInitializationFailure() {
        Challenges challenges = new Challenges();
        Accounts accounts = new Accounts();
        Registrations registrations = new Registrations();
        Sessions sessions = new Sessions();
        HmacSha256ChallengeSecretHasher hasher = new HmacSha256ChallengeSecretHasher("test-pepper");
        AtomicInteger initializationCalls = new AtomicInteger();
        UserGateway gateway = userId -> {
            if (initializationCalls.incrementAndGet() == 1) {
                throw new ApplicationException(ApplicationError.APP_USER_INITIALIZATION_FAILED);
            }
        };
        Services services = services(challenges, accounts, registrations, sessions, hasher, gateway);
        IssueAuthChallengeCommand issue = new IssueAuthChallengeCommand(
                AuthChallengeType.SMS_OTP, AuthChallengeScene.LOGIN, "13800138000");

        IssueAuthChallengeOutput firstIssue = services.challengeService().execute(issue);
        IssueAuthChallengeOutput repeatedIssue = services.challengeService().execute(issue);
        assertEquals(firstIssue.challengeId(), repeatedIssue.challengeId());
        assertTrue(repeatedIssue.reused());

        MobileOtpAuthenticationCommand login =
                login(firstIssue.challengeId(), "123456", "device-1");
        ApplicationException failure = assertThrows(
                ApplicationException.class, () -> services.loginService().authenticate(login));
        assertEquals(
                ApplicationError.APP_USER_INITIALIZATION_FAILED.errorCode(),
                failure.getErrorCode());
        RegistrationProcess process = registrations
                .findByChallengeId(new AuthChallengeId(firstIssue.challengeId()))
                .orElseThrow();
        assertEquals(RegistrationProcessStatus.AUTH_ACCOUNT_CREATED, process.getStatus());
        assertEquals(1, process.getRetryCount());
        assertNotNull(process.getLastFailedAt());

        Long reservedUserId = accounts.findByCredential(CredentialKey.mobile(new LoginMobile("13800138000")))
                .orElseThrow().userId().value();
        MobileOtpAuthenticationOutput completed = services.loginService().authenticate(login);
        assertEquals(reservedUserId, completed.userId());
        assertTrue(completed.fromRegistrationFlow());
        assertFalse(completed.replayed());
        assertEquals(2, initializationCalls.get());

        MobileOtpAuthenticationOutput replayed = services.loginService().authenticate(login);
        assertEquals(completed.sessionId(), replayed.sessionId());
        assertTrue(replayed.replayed());
        assertThrows(DomainException.class, () -> services.loginService().authenticate(
                login(firstIssue.challengeId(), "000000", "device-1")));

        IssueAuthChallengeOutput nextChallenge = services.challengeService().execute(issue);
        assertFalse(nextChallenge.reused());
        MobileOtpAuthenticationOutput existingLogin = services.loginService().authenticate(
                login(nextChallenge.challengeId(), "123456", "device-2"));
        assertEquals(reservedUserId, existingLogin.userId());
        assertFalse(existingLogin.fromRegistrationFlow());
        System.out.printf(
                "registration resumed: userId=%d, accountId=%d, sessionId=%s, retries=%d%n",
                completed.userId(), completed.authAccountId(), completed.sessionId(), process.getRetryCount());
    }

    @Test
    void shouldRejectConsumedChallengeAfterOriginalExpiry() {
        HmacSha256ChallengeSecretHasher hasher = new HmacSha256ChallengeSecretHasher("test-pepper");
        Instant now = Instant.now();
        AuthChallenge expired = AuthChallenge.restore(
                new AuthChallengeId(1L), AuthChallengeType.SMS_OTP,
                new ChallengeTarget("13800138000"), AuthChallengeScene.LOGIN,
                hasher.hash(new AuthChallengeId(1L), "123456"), AuthChallengeStatus.CONSUMED,
                now.minusSeconds(1), now.minusSeconds(240),
                1, now.minusSeconds(10),
                ChallengeConsumerType.REGISTRATION_PROCESS, "1",
                now.minusSeconds(300), now.minusSeconds(10));

        assertThrows(DomainException.class, () -> expired.verify(
                AuthChallengeType.SMS_OTP, AuthChallengeScene.LOGIN,
                hasher.hash(new AuthChallengeId(1L), "123456"),
                now));
        assertEquals(AuthChallengeStatus.EXPIRED, expired.getStatus());
        System.out.printf("expired challenge rejected: challengeId=%d, status=%s%n",
                expired.id().value(), expired.getStatus());
    }

    @Test
    void shouldRejectReplayWhenLoginSessionHasExpired() {
        Challenges challenges = new Challenges();
        Accounts accounts = new Accounts();
        Registrations registrations = new Registrations();
        Sessions sessions = new Sessions();
        Instant now = Instant.now();
        SessionId sessionId = new SessionId("session-1");
        UserId userId = new UserId(1L);
        sessions.save(LoginSession.create(
                sessionId, userId, new AuthAccountId(1L), new CredentialId(1L),
                LoginScene.MOBILE_LOGIN,
                new Device("device", "PHONE", "phone"), new Client("app", "IOS", "1.0"),
                now.minusSeconds(60),
                now.minusSeconds(1)));
        MobileOtpLoginTransactionService transactionalService = transactionalService(
                challenges, accounts, registrations, sessions,
                new HmacSha256ChallengeSecretHasher("pepper"));

        assertThrows(DomainException.class, () -> transactionalService.replayLogin(sessionId, true));
        System.out.printf("expired login session replay rejected: sessionId=%s%n", sessionId.value());
    }

    private Services services(
            Challenges challenges, Accounts accounts, Registrations registrations,
            Sessions sessions, HmacSha256ChallengeSecretHasher hasher, UserGateway gateway
    ) {
        MobileOtpLoginTransactionService transactionalService = transactionalService(
                challenges, accounts, registrations, sessions, hasher);
        return new Services(
                new AuthChallengeIssueCommandService(
                        challenges,
                        () -> "123456",
                        hasher,
                        (type, target, secret) -> {
                        },
                        AuthChallengeIssueLock.direct(),
                        () -> new AuthChallengeIssuePolicy(
                                java.time.Duration.ofMinutes(5),
                                java.time.Duration.ofSeconds(60)),
                        java.time.Clock.systemUTC()),
                new MobileOtpAuthenticationProcess(
                        challenges, registrations, accounts,
                        MobileOtpLoginLock.direct(), transactionalService,
                        (ignoredUserId, ignoredChannelCode) -> { }, gateway));
    }

    private MobileOtpLoginTransactionService transactionalService(
            Challenges challenges, Accounts accounts, Registrations registrations,
            Sessions sessions, HmacSha256ChallengeSecretHasher hasher
    ) {
        AtomicLong userIds = new AtomicLong(100_000);
        AtomicLong credentialIds = new AtomicLong(10_000);
        return new MobileOtpLoginTransactionService(
                challenges, registrations, accounts, sessions,
                () -> new UserId(userIds.incrementAndGet()),
                () -> new CredentialId(credentialIds.incrementAndGet()),
                hasher,
                new AuthenticationDomainService(),
                events -> {
                },
                java.time.Clock.systemUTC(),
                java.time.Duration.ofDays(30));
    }

    private MobileOtpAuthenticationCommand login(
            Long challengeId,
            String code,
            String deviceId
    ) {
        return new MobileOtpAuthenticationCommand(
                challengeId,
                code,
                deviceId,
                "PHONE",
                "Test phone",
                "app",
                "IOS",
                "1.0",
                "DIRECT");
    }

    private record Services(
            AuthChallengeIssueCommandService challengeService,
            MobileOtpAuthenticationProcess loginService
    ) {
    }
}
