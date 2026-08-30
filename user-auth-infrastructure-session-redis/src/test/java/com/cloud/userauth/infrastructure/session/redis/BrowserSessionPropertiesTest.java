package com.cloud.userauth.infrastructure.session.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.infrastructure.session.redis.config.BrowserSessionProperties;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Map;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class BrowserSessionPropertiesTest {
    private static final Validator VALIDATOR = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    @Test
    void shouldBindBrowserSessionProperties() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.browser-session.namespace", "user-auth",
                "user-auth.authentication.browser-session.idle-ttl", "20m",
                "user-auth.authentication.browser-session.absolute-ttl", "6h",
                "user-auth.authentication.browser-session.renewal-threshold", "5m"));

        BrowserSessionProperties properties = new Binder(source)
                .bind(
                        "user-auth.authentication.browser-session",
                        Bindable.of(BrowserSessionProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals("user-auth", properties.getNamespace());
        assertEquals(Duration.ofMinutes(20), properties.getIdleTtl());
        assertEquals(Duration.ofHours(6), properties.getAbsoluteTtl());
        assertEquals(Duration.ofMinutes(5), properties.getRenewalThreshold());
        assertTrue(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectRenewalThresholdLongerThanIdleTtl() {
        BrowserSessionProperties properties = new BrowserSessionProperties();
        properties.setIdleTtl(Duration.ofMinutes(5));
        properties.setAbsoluteTtl(Duration.ofHours(8));
        properties.setRenewalThreshold(Duration.ofMinutes(6));

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectIdleTtlLongerThanAbsoluteTtl() {
        BrowserSessionProperties properties = new BrowserSessionProperties();
        properties.setIdleTtl(Duration.ofHours(9));
        properties.setAbsoluteTtl(Duration.ofHours(8));
        properties.setRenewalThreshold(Duration.ofMinutes(5));

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }
}
