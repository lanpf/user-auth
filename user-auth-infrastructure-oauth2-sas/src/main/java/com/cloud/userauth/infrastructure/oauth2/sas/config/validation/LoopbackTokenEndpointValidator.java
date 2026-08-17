package com.cloud.userauth.infrastructure.oauth2.sas.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import org.springframework.util.StringUtils;

public final class LoopbackTokenEndpointValidator
        implements ConstraintValidator<LoopbackTokenEndpoint, String> {

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context
    ) {
        if (!StringUtils.hasText(value)) {
            return true;
        }
        try {
            URI endpoint = URI.create(value);
            return ("http".equalsIgnoreCase(endpoint.getScheme())
                    || "https".equalsIgnoreCase(endpoint.getScheme()))
                    && "/oauth2/token".equals(endpoint.getPath())
                    && endpoint.getHost() != null
                    && endpoint.getUserInfo() == null
                    && endpoint.getQuery() == null
                    && endpoint.getFragment() == null
                    && InetAddress.getByName(endpoint.getHost()).isLoopbackAddress();
        } catch (RuntimeException | IOException exception) {
            return false;
        }
    }
}
