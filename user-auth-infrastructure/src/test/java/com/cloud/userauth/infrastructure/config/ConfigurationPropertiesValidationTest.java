package com.cloud.userauth.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.api.authentication.OAuth2Scope;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class ConfigurationPropertiesValidationTest {
    private static final Validator VALIDATOR =
            Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(new ParameterMessageInterpolator())
                    .buildValidatorFactory()
                    .getValidator();

    @Test
    void shouldAcceptExplicitDurations() {
        AuthChallengeProperties challengeProperties = validChallengeProperties();
        LoginSessionProperties sessionProperties = new LoginSessionProperties();
        sessionProperties.setTtl(Duration.ofDays(30));
        ExternalIdentityProperties externalIdentityProperties = new ExternalIdentityProperties();
        externalIdentityProperties.getLoginAttempt().setTtl(Duration.ofMinutes(10));

        assertTrue(VALIDATOR.validate(challengeProperties).isEmpty());
        assertTrue(VALIDATOR.validate(sessionProperties).isEmpty());
        assertTrue(VALIDATOR.validate(externalIdentityProperties).isEmpty());
    }

    @Test
    void shouldRejectDurationsBelowBusinessMinimum() {
        AuthChallengeProperties challengeProperties = validChallengeProperties();
        challengeProperties.getIssuePolicy().setTtl(Duration.ZERO);
        LoginSessionProperties sessionProperties = new LoginSessionProperties();
        sessionProperties.setTtl(Duration.ofHours(23));
        ExternalIdentityProperties externalIdentityProperties = new ExternalIdentityProperties();
        externalIdentityProperties.getLoginAttempt().setTtl(Duration.ZERO);

        assertFalse(VALIDATOR.validate(challengeProperties).isEmpty());
        assertFalse(VALIDATOR.validate(sessionProperties).isEmpty());
        assertFalse(VALIDATOR.validate(externalIdentityProperties).isEmpty());
    }

    @Test
    void shouldRejectReuseWindowGreaterThanChallengeTtl() {
        AuthChallengeProperties properties = validChallengeProperties();
        properties.getIssuePolicy().setTtl(Duration.ofSeconds(30));
        properties.getIssuePolicy().setReuseWindow(Duration.ofSeconds(31));

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectInvalidNonProductionFixedCode() {
        NonProductionAuthChallengeProperties properties =
                new NonProductionAuthChallengeProperties();
        properties.setFixedCode("12345");

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldBindNestedChallengeAndExternalIdentityPolicies() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.challenge.pepper", "test-pepper",
                "user-auth.authentication.challenge.issue-policy.ttl", "3m",
                "user-auth.authentication.challenge.issue-policy.reuse-window", "30s",
                "user-auth.authentication.external-identity.login-attempt.ttl", "8m",
                "user-auth.authentication.external-identity.issuer-policies.WECHAT_MINI_PROGRAM.trust-verified-mobile", true));
        Binder binder = new Binder(source);

        AuthChallengeProperties challenge = binder.bind(
                "user-auth.authentication.challenge", Bindable.of(AuthChallengeProperties.class))
                .orElseThrow(IllegalStateException::new);
        ExternalIdentityProperties externalIdentity = binder.bind(
                "user-auth.authentication.external-identity", Bindable.of(ExternalIdentityProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertTrue(VALIDATOR.validate(challenge).isEmpty());
        assertTrue(VALIDATOR.validate(externalIdentity).isEmpty());
        assertTrue(externalIdentity.getIssuerPolicies()
                .get("WECHAT_MINI_PROGRAM").getTrustVerifiedMobile());
    }

    @Test
    void shouldBindClientAppRenewalPoliciesOutsideSasConfiguration() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.client-apps.mini-program.renewal-policy",
                "EXTERNAL_AUTHORIZATION_CODE",
                "user-auth.authentication.client-apps.mini-program.oauth2-scopes[0]",
                "app",
                "user-auth.authentication.client-apps.app.renewal-policy",
                "REFRESH_TOKEN_ROTATION",
                "user-auth.authentication.client-apps.app.oauth2-scopes[0]",
                "app"));

        ClientAppRegistryProperties properties = new Binder(source)
                .bind("user-auth.authentication", Bindable.of(ClientAppRegistryProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals(
                ClientRenewalPolicy.EXTERNAL_AUTHORIZATION_CODE,
                properties.getClientApps().get("mini-program").getRenewalPolicy());
        assertEquals(
                ClientRenewalPolicy.REFRESH_TOKEN_ROTATION,
                properties.getClientApps().get("app").getRenewalPolicy());
        assertEquals(
                Set.of(OAuth2Scope.APP),
                properties.getClientApps().get("app").getOauth2Scopes());
        assertTrue(VALIDATOR.validate(properties).isEmpty());
    }

    private static AuthChallengeProperties validChallengeProperties() {
        AuthChallengeProperties properties = new AuthChallengeProperties();
        properties.setPepper("test-pepper");
        properties.getIssuePolicy().setTtl(Duration.ofMinutes(5));
        properties.getIssuePolicy().setReuseWindow(Duration.ofMinutes(1));
        return properties;
    }
}
