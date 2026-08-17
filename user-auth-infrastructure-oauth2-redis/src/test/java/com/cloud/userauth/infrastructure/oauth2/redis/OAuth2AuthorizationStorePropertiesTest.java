package com.cloud.userauth.infrastructure.oauth2.redis;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.cloud.userauth.infrastructure.oauth2.redis.config.OAuth2AuthorizationStoreProperties;
import jakarta.validation.Validation;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;

class OAuth2AuthorizationStorePropertiesTest {

    @Test
    void shouldRejectBlankScene() {
        OAuth2AuthorizationStoreProperties properties = new OAuth2AuthorizationStoreProperties();
        properties.setScene(" ");

        assertFalse(Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator()
                .validate(properties).isEmpty());
    }
}
