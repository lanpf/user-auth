package com.cloud.userauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.application.login.external.ExternalAttemptLoginOutput;
import com.cloud.userauth.application.login.external.ExternalCredentialBinding;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginOutput;
import com.cloud.userauth.application.login.external.ExternalProofLoginCommand;
import com.cloud.userauth.application.login.external.ExternalProofLoginCommandService;
import com.cloud.userauth.application.login.external.ExternalAuthenticationCommand;
import com.cloud.userauth.application.login.external.ExternalAuthenticationOutput;
import com.cloud.userauth.application.login.external.ExternalAuthenticationProcess;
import com.cloud.userauth.application.login.external.ExternalLoginTransactionService;
import com.cloud.userauth.application.login.external.ExternalAttemptLoginCommand;
import com.cloud.userauth.application.login.external.ExternalAttemptLoginCommandService;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginOutput;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.LoginTokenIssuer;
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
        ExternalAttemptLoginOutput preLogin = fixture.preLogin();

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
        ExternalAttemptLoginOutput preLogin = fixture.preLogin();
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
        Fixture fixture = fixture(true);
        ExternalAttemptLoginOutput preLogin = fixture.directPreLogin();

        assertFalse(preLogin.mobileVerificationRequired());
        ExternalAuthenticationOutput login = fixture.directLogin(preLogin);

        AuthAccount account = fixture.accounts.findById(
                new com.cloud.userauth.domain.authentication.account.AuthAccountId(login.authAccountId())).orElseThrow();
        assertEquals(1, account.credentials().size());
        assertTrue(account.credentials().stream()
                .allMatch(credential -> credential.getCredentialType() == CredentialType.MOBILE));
    }

    @Test
    void shouldCompleteExternalIdentityAndMobileProofInOneLoginRequest() {
        Fixture fixture = fixture(true);

        ExternalLoginOutput login = fixture.loginWithExternalProof();

        AuthAccount account = fixture.accounts.findById(
                new com.cloud.userauth.domain.authentication.account.AuthAccountId(
                        login.authAccountId())).orElseThrow();
        assertEquals(2, account.credentials().size());
        assertTrue(account.credentials().stream()
                .anyMatch(credential -> credential.getCredentialType() == CredentialType.MOBILE));
        assertTrue(account.credentials().stream()
                .anyMatch(credential -> credential.getCredentialType() == CredentialType.EXTERNAL));
        System.out.printf(
                "single-step external proof login completed: userId=%d, authAccountId=%d, "
                        + "sessionId=%s, credentialCount=%d%n",
                login.userId(), login.authAccountId(), login.sessionId(),
                account.credentials().size());
    }

    @Test
    void shouldRejectSingleStepExternalProofWithoutVerifiedMobile() {
        Fixture fixture = fixture(true, false);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                fixture::loginWithExternalProof);

        assertEquals(ApplicationError.APP_LOGIN_REJECTED.errorCode(), exception.getErrorCode());
    }

    @Test
    void shouldRejectSingleStepExternalProofWhenIssuerMobileIsNotTrusted() {
        Fixture fixture = fixture(false, true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                fixture::loginWithExternalProof);

        assertEquals(ApplicationError.APP_LOGIN_REJECTED.errorCode(), exception.getErrorCode());
    }

    private static Fixture fixture(boolean trustMobile) {
        return fixture(trustMobile, true);
    }

    private static Fixture fixture(boolean trustMobile, boolean verifiedMobile) {
        Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        Accounts accounts = new Accounts();
        Challenges challenges = new Challenges();
        LoginAttempts loginAttempts = new LoginAttempts();
        Sessions sessions = new Sessions();
        AtomicLong users = new AtomicLong(1000);
        AtomicLong credentials = new AtomicLong(2000);
        ExternalIdentityDomainService externalDomain =
                new ExternalIdentityDomainService();
        var verifier = (com.cloud.userauth.application.port.ExternalIdentityVerifierRegistry)
                (issuer, proofType, payload) -> new ExternalIdentity(
                        WECHAT, new Principal("openid-1"),
                        verifiedMobile ? MOBILE : null, verifiedMobile);
        var trustPolicies = (com.cloud.userauth.application.port.IssuerMobileTrustPolicyProvider)
                issuer -> new IssuerMobileTrustPolicy(issuer, trustMobile);
        ExternalAttemptLoginCommandService preLoginService = new ExternalAttemptLoginCommandService(
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
                new AuthenticationDomainService();
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
                        new CredentialDomainService(accounts),
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
            ExternalAttemptLoginCommandService preLoginService,
            ExternalAuthenticationProcess authenticationProcess
    ) {
        ExternalAttemptLoginOutput preLogin() {
            return preLoginService.execute(new ExternalAttemptLoginCommand(
                    WECHAT.code(),
                    ProofType.AUTHORIZATION_CODE,
                    Map.of("loginCode", "one-time-code")));
        }

        ExternalAttemptLoginOutput directPreLogin() {
            return preLoginService.acceptIdentity(new ExternalIdentity(
                    new CredentialIssuer("PARTNER_A", CredentialIssuerType.TRUSTED_PARTNER),
                    new Principal("order-1"), MOBILE, true));
        }

        ExternalAuthenticationOutput login(
                ExternalAttemptLoginOutput preLogin,
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
                    "1.0",
                    "DIRECT",
                    true));
        }

        ExternalAuthenticationOutput directLogin(ExternalAttemptLoginOutput preLogin) {
            return authenticationProcess.authenticate(new ExternalAuthenticationCommand(
                    preLogin.loginAttemptId(), null, null, "device-1", "SERVICE", "partner",
                    "partner-service", "SERVICE", "1.0", "PARTNER_A", false));
        }

        ExternalLoginOutput loginWithExternalProof() {
            LoginTokenIssuer tokenIssuer = new LoginTokenIssuer() {
                @Override
                public MobileOtpLoginOutput issueMobileOtpLogin(MobileOtpLoginCommand command) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public ExternalLoginOutput issueExternalLogin(
                        ExternalLoginCommand command,
                        ExternalCredentialBinding credentialBinding
                ) {
                    assertEquals(ExternalCredentialBinding.BIND, credentialBinding);
                    ExternalAuthenticationOutput authenticated = authenticationProcess.authenticate(
                            new ExternalAuthenticationCommand(
                                    command.loginAttemptId(),
                                    command.challengeId(),
                                    command.code(),
                                    command.deviceId(),
                                    command.deviceType(),
                                    command.deviceName(),
                                    command.clientAppId(),
                                    command.clientPlatform(),
                                    command.clientVersion(),
                                    command.channelCode(),
                                    true));
                    return new ExternalLoginOutput(
                            "Bearer", "access-token", null, 900L, "app",
                            authenticated.userId(), authenticated.authAccountId(),
                            authenticated.sessionId());
                }
            };
            ExternalProofLoginCommandService service = new ExternalProofLoginCommandService(
                    preLoginService, tokenIssuer);
            return service.execute(new ExternalProofLoginCommand(
                    WECHAT.code(),
                    ProofType.AUTHORIZATION_CODE,
                    Map.of("loginCode", "login-code", "phoneCode", "phone-code"),
                    "device-1",
                    "PHONE",
                    "phone",
                    "mini-program",
                    "WECHAT_MINI_PROGRAM",
                    "1.0",
                    "DIRECT"));
        }
    }
}
