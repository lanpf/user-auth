package com.cloud.userauth.infrastructure.external.wechat.miniprogram.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Map;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class WechatMiniProgramPropertiesTest {
    private static final Validator VALIDATOR =
            Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(
                            new ParameterMessageInterpolator())
                    .buildValidatorFactory()
                    .getValidator();

    @Test
    void shouldRequireCredentialsWhenPropertiesAreEnabled() {
        WechatMiniProgramProperties properties =
                bind(Map.of("enabled", "true"));

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldAcceptValidConfigurationAndRejectShortTimeout() {
        WechatMiniProgramProperties properties =
                bind(Map.of(
                        "enabled", "true",
                        "app-id", "app-id",
                        "app-secret", "app-secret"));

        assertTrue(VALIDATOR.validate(properties).isEmpty());

        properties = bind(Map.of(
                "enabled", "true",
                "app-id", "app-id",
                "app-secret", "app-secret",
                "rest-client.read-timeout", Duration.ZERO));

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldBindSharedTimeoutNamesAndDefaults() {
        WechatMiniProgramProperties defaults = bind(Map.of(
                "enabled", "true",
                "app-id", "app-id",
                "app-secret", "app-secret"));
        WechatMiniProgramProperties configured = bind(Map.of(
                "enabled", "true",
                "app-id", "app-id",
                "app-secret", "app-secret",
                "rest-client.connect-timeout", "3s",
                "rest-client.read-timeout", "8s"));

        assertTrue(defaults.getRestClient().getConnectTimeout()
                .equals(Duration.ofSeconds(2)));
        assertTrue(defaults.getRestClient().getReadTimeout()
                .equals(Duration.ofSeconds(5)));
        assertTrue(configured.getRestClient().getConnectTimeout()
                .equals(Duration.ofSeconds(3)));
        assertTrue(configured.getRestClient().getReadTimeout()
                .equals(Duration.ofSeconds(8)));
    }

    private static WechatMiniProgramProperties bind(
            Map<String, Object> values
    ) {
        MapConfigurationPropertySource source =
                new MapConfigurationPropertySource();
        values.forEach((name, value) -> source.put(
                "user-auth.authentication.external-identity.wechat-mini-program." + name,
                value));
        return new Binder(source)
                .bind(
                        "user-auth.authentication.external-identity.wechat-mini-program",
                        Bindable.of(WechatMiniProgramProperties.class))
                .orElseThrow(IllegalStateException::new);
    }
}
