package com.cloud.userauth.infrastructure.session.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.infrastructure.session.redis.config.H5SessionProperties;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Map;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class H5SessionPropertiesTest {
    private static final Validator VALIDATOR = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    @Test
    void shouldBindH5SessionProperties() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.h5-session.idle-ttl", "20m",
                "user-auth.authentication.h5-session.absolute-ttl", "6h",
                "user-auth.authentication.h5-session.renewal-threshold", "5m"));

        H5SessionProperties properties = new Binder(source)
                .bind("user-auth.authentication.h5-session", Bindable.of(H5SessionProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals(Duration.ofMinutes(20), properties.getIdleTtl());
        assertEquals(Duration.ofHours(6), properties.getAbsoluteTtl());
        assertEquals(Duration.ofMinutes(5), properties.getRenewalThreshold());
        assertTrue(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectRenewalThresholdLongerThanIdleTtl() {
        H5SessionProperties properties = new H5SessionProperties();
        properties.setIdleTtl(Duration.ofMinutes(5));
        properties.setRenewalThreshold(Duration.ofMinutes(6));

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }

    @Test
    void shouldRejectIdleTtlLongerThanAbsoluteTtl() {
        H5SessionProperties properties = new H5SessionProperties();
        properties.setIdleTtl(Duration.ofHours(9));
        properties.setAbsoluteTtl(Duration.ofHours(8));

        assertFalse(VALIDATOR.validate(properties).isEmpty());
    }
}
