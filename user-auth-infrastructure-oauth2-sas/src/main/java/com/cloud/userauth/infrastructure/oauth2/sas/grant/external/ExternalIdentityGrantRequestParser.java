package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasOAuth2RequestMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class ExternalIdentityGrantRequestParser {
    private final Validator validator;

    public ExternalIdentityGrantRequest parse(HttpServletRequest request) {
        ExternalIdentityGrantRequest parsed = new ExternalIdentityGrantRequest(
                single(request, ExternalIdentityGrantParameterNames.LOGIN_ATTEMPT_ID),
                longValue(single(request, ExternalIdentityGrantParameterNames.CHALLENGE_ID)),
                single(request, ExternalIdentityGrantParameterNames.CODE),
                single(request, OAuth2ParameterNames.SCOPE),
                single(request, ExternalIdentityGrantParameterNames.DEVICE_ID),
                single(request, ExternalIdentityGrantParameterNames.DEVICE_TYPE),
                single(request, ExternalIdentityGrantParameterNames.DEVICE_NAME),
                single(request, ExternalIdentityGrantParameterNames.CLIENT_APP_ID),
                single(request, ExternalIdentityGrantParameterNames.CLIENT_PLATFORM),
                single(request, ExternalIdentityGrantParameterNames.CLIENT_VERSION),
                single(request, ExternalIdentityGrantParameterNames.CHANNEL_CODE),
                booleanValue(single(request, ExternalIdentityGrantParameterNames.BIND_EXTERNAL_IDENTITY)));
        Set<ConstraintViolation<ExternalIdentityGrantRequest>> violations = validator.validate(parsed);
        if (!CollectionUtils.isEmpty(violations)) {
            throw oauth2(SasOAuth2RequestMessages.invalid(
                    violations.iterator().next().getPropertyPath().toString()));
        }
        return parsed;
    }

    private static String single(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if (values == null) {
            return null;
        }
        if (values.length != 1) {
            throw oauth2(SasOAuth2RequestMessages.atMostOnce(name));
        }
        return StringUtils.hasText(values[0]) ? values[0] : null;
    }

    private static Long longValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw oauth2(SasOAuth2RequestMessages.integerRequired(value));
        }
    }

    private static boolean booleanValue(String value) {
        if (value == null) {
            return true;
        }
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return true;
        }
        if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            return false;
        }
        throw oauth2(SasOAuth2RequestMessages.booleanRequired(value));
    }

    private static OAuth2AuthenticationException oauth2(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, description, null));
    }
}
