package com.cloud.userauth.infrastructure.session.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.infrastructure.session.redis.config.SessionHandoffTicketProperties;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Map;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class SessionHandoffTicketPropertiesTest {
    private static final Validator VALIDATOR = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    @Test
    void shouldBindSessionHandoffTicketProperties() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "user-auth.authentication.session-handoff.ttl", "45s"));

        SessionHandoffTicketProperties properties = new Binder(source)
                .bind("user-auth.authentication.session-handoff", Bindable.of(SessionHandoffTicketProperties.class))
                .orElseThrow(IllegalStateException::new);

        assertEquals(Duration.ofSeconds(45), properties.getTtl());
        assertTrue(VALIDATOR.validate(properties).isEmpty());
    }
}
