package com.cloud.userauth.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class AccessTokenPropertiesTest {
    private static final Validator VALIDATOR = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    @Test
    void shouldBindAccessTokenProviderAndTtl() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.access-token.provider", "session-token",
                "user-auth.authentication.access-token.ttl", "20m"));

        AccessTokenProperties properties = new Binder(source)
                .bind("user-auth.authentication.access-token",
                        Bindable.of(AccessTokenProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals(AccessTokenProperties.Provider.SESSION_TOKEN, properties.getProvider());
        assertEquals(Duration.ofMinutes(20), properties.getTtl());
        assertTrue(VALIDATOR.validate(properties).isEmpty());
    }
}
