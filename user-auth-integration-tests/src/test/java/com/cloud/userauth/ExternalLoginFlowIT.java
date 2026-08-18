package com.cloud.userauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.application.login.external.ExternalAuthenticationCommand;
import com.cloud.userauth.application.login.external.ExternalAuthenticationOutput;
import com.cloud.userauth.application.login.external.ExternalAuthenticationProcess;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptOutput;
import com.cloud.userauth.application.login.external.ExternalLoginTransactionService;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommand;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommandService;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeSecretHash;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuerType;
import com.cloud.userauth.domain.authentication.credential.CredentialType;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.IssuerMobileTrustPolicy;
import com.cloud.userauth.domain.authentication.external.ProofType;
import com.cloud.userauth.domain.authentication.service.AuthenticationDomainService;
import com.cloud.userauth.domain.authentication.service.CredentialDomainService;
import com.cloud.userauth.domain.authentication.service.ExternalIdentityDomainService;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.security.HmacSha256ChallengeSecretHasher;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.Accounts;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.Challenges;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.LoginAttempts;
import com.cloud.userauth.testsupport.UserAuthTestRepositories.Sessions;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class ExternalLoginFlowIT {
    private static final CredentialIssuer WECHAT =
            new CredentialIssuer("WECHAT", CredentialIssuerType.PUBLIC_THIRD_PARTY);
    private static final LoginMobile MOBILE = new LoginMobile("13800138000");

    @Test
    void shouldCreateMobileAndExternalCredentialsForTrustedMobile() {
        Fixture fixture = fixture(true);
        ExternalLoginAttemptOutput preLogin = fixture.preLogin();

        assertFalse(preLogin.mobileVerificationRequired());
        ExternalAuthenticationOutput login = fixture.login(preLogin, null, null);

        AuthAccount account = fixture.accounts.findById(
                new com.cloud.userauth.domain.authentication.account.AuthAccountId(login.authAccountId())).orElseThrow();
        assertEquals(2, account.credentials().size());
        assertTrue(account.credentials().stream()
                .anyMatch(credential -> credential.getCredentialType() == CredentialType.MOBILE));
        assertTrue(account.credentials().stream()
                .anyMatch(credential -> credential.getCredentialType() == CredentialType.EXTERNAL));
        assertEquals(LoginScene.EXTERNAL_LOGIN,
                fixture.sessions.findById(
                        new com.cloud.userauth.domain.authentication.session.SessionId(login.sessionId()))
                        .orElseThrow().getLoginScene());
        System.out.printf(
                "external login completed: trustedMobile=true, mobileVerificationRequired=%s, "
                        + "userId=%d, authAccountId=%d, sessionId=%s, credentialCount=%d%n",
                preLogin.mobileVerificationRequired(),
                login.userId(),
                login.authAccountId(),
                login.sessionId(),
                account.credentials().size());
    }

    @Test
    void shouldRequireChallengeForUntrustedMobileAndThenCreateBothCredentials() {
        Fixture fixture = fixture(false);
        ExternalLoginAttemptOutput preLogin = fixture.preLogin();
        assertTrue(preLogin.mobileVerificationRequired());

        long challengeId = 9001L;
        Instant now = fixture.clock.instant();
        ChallengeSecretHash secret = fixture.hasher.hash(
                new com.cloud.userauth.domain.authentication.challenge.AuthChallengeId(challengeId), "123456");
        fixture.challenges.save(AuthChallenge.issue(
                new com.cloud.userauth.domain.authentication.challenge.AuthChallengeId(challengeId),
                AuthChallengeType.SMS_OTP,
                ChallengeTarget.of(AuthChallengeType.SMS_OTP, MOBILE.value()),
                AuthChallengeScene.COMPLETE_EXTERNAL_LOGIN,
                secret,
                now,
                now.plus(Duration.ofMinutes(5)),
                now.plus(Duration.ofMinutes(1))));

        ExternalAuthenticationOutput login =
                fixture.login(preLogin, challengeId, "123456");
        AuthAccount account = fixture.accounts.findById(
                new com.cloud.userauth.domain.authentication.account.AuthAccountId(login.authAccountId())).orElseThrow();
        assertEquals(2, account.credentials().size());
        System.out.printf(
                "external login completed: trustedMobile=false, mobileVerificationRequired=%s, "
                        + "challengeId=%d, userId=%d, authAccountId=%d, sessionId=%s, "
                        + "credentialCount=%d%n",
                preLogin.mobileVerificationRequired(),
                challengeId,
                login.userId(),
                login.authAccountId(),
                login.sessionId(),
                account.credentials().size());
    }

    @Test
    void shouldNotBindExternalOrderProofForDirectLogin() {
        Fixture fixture = fixture(false);
        ExternalLoginAttemptOutput preLogin = fixture.directPreLogin();

        assertFalse(preLogin.mobileVerificationRequired());
        ExternalAuthenticationOutput login = fixture.directLogin(preLogin);

        AuthAccount account = fixture.accounts.findById(
                new com.cloud.userauth.domain.authentication.account.AuthAccountId(login.authAccountId())).orElseThrow();
        assertEquals(1, account.credentials().size());
        assertTrue(account.credentials().stream()
                .allMatch(credential -> credential.getCredentialType() == CredentialType.MOBILE));
    }

    private static Fixture fixture(boolean trustMobile) {
        Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        Accounts accounts = new Accounts();
        Challenges challenges = new Challenges();
        LoginAttempts loginAttempts = new LoginAttempts();
        Sessions sessions = new Sessions();
        AtomicLong users = new AtomicLong(1000);
        AtomicLong credentials = new AtomicLong(2000);
        AtomicLong events = new AtomicLong(3000);
        var eventIds = (com.cloud.framework.domain.DomainEventIdGenerator)
                () -> new DomainEventId(events.incrementAndGet());
        ExternalIdentityDomainService externalDomain =
                new ExternalIdentityDomainService(eventIds);
        var verifier = (com.cloud.userauth.application.port.ExternalIdentityVerifierRegistry)
                (issuer, proofType, payload) -> new ExternalIdentity(
                        WECHAT, new Principal("openid-1"), MOBILE, true, "tester");
        var trustPolicies = (com.cloud.userauth.application.port.IssuerMobileTrustPolicyProvider)
                issuer -> new IssuerMobileTrustPolicy(issuer, trustMobile);
        ExternalLoginAttemptCommandService preLoginService = new ExternalLoginAttemptCommandService(
                verifier,
                trustPolicies,
                accounts,
                loginAttempts,
                externalDomain,
                ignored -> { },
                clock,
                Duration.ofMinutes(10));
        HmacSha256ChallengeSecretHasher hasher =
                new HmacSha256ChallengeSecretHasher("test-pepper");
        AuthenticationDomainService authenticationDomain =
                new AuthenticationDomainService(eventIds);
        ExternalLoginTransactionService transactionService =
                new ExternalLoginTransactionService(
                        loginAttempts,
                        challenges,
                        accounts,
                        sessions,
                        () -> new UserId(users.incrementAndGet()),
                        () -> new CredentialId(credentials.incrementAndGet()),
                        hasher,
                        authenticationDomain,
                        new CredentialDomainService(accounts, eventIds),
                        externalDomain,
                        ignored -> { },
                        clock,
                        Duration.ofDays(30));
        ExternalAuthenticationProcess authenticationProcess =
                new ExternalAuthenticationProcess(
                        transactionService,
                        (ignoredUserId, ignoredChannelCode) -> { },
                        ignored -> { });
        return new Fixture(
                clock, accounts, challenges, sessions, hasher,
                preLoginService, authenticationProcess);
    }

    private record Fixture(
            Clock clock,
            Accounts accounts,
            Challenges challenges,
            Sessions sessions,
            HmacSha256ChallengeSecretHasher hasher,
            ExternalLoginAttemptCommandService preLoginService,
            ExternalAuthenticationProcess authenticationProcess
    ) {
        ExternalLoginAttemptOutput preLogin() {
            return preLoginService.execute(new ExternalLoginAttemptCommand(
                    WECHAT.code(),
                    ProofType.AUTHORIZATION_CODE,
                    Map.of("loginCode", "one-time-code")));
        }

        ExternalLoginAttemptOutput directPreLogin() {
            return preLoginService.acceptTrustedIdentity(new ExternalIdentity(
                    new CredentialIssuer("PARTNER_A", CredentialIssuerType.TRUSTED_PARTNER),
                    new Principal("order-1"), MOBILE, true, null));
        }

        ExternalAuthenticationOutput login(
                ExternalLoginAttemptOutput preLogin,
                Long challengeId,
                String code
        ) {
            return authenticationProcess.authenticate(new ExternalAuthenticationCommand(
                    preLogin.loginAttemptId(),
                    challengeId,
                    code,
                    "device-1",
                    "PHONE",
                    "phone",
                    "app",
                    "IOS",
                    "1.0"));
        }

        ExternalAuthenticationOutput directLogin(ExternalLoginAttemptOutput preLogin) {
            return authenticationProcess.authenticate(new ExternalAuthenticationCommand(
                    preLogin.loginAttemptId(), null, null, "device-1", "SERVICE", "partner",
                    "partner-service", "SERVICE", "1.0", false));
        }
    }
}
