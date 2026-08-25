package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

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

/** 从 OAuth2 Token Endpoint 的 Servlet 请求中解析并校验 mobile_otp Grant。 */
@RequiredArgsConstructor
public final class MobileOtpGrantRequestParser {
    private final Validator validator;

    public MobileOtpGrantRequest parse(HttpServletRequest request) {
        String challengeId = singleValue(
                request,
                MobileOtpGrantParameterNames.CHALLENGE_ID);
        MobileOtpGrantRequest grantRequest = new MobileOtpGrantRequest(
                parseChallengeId(challengeId),
                singleValue(request, MobileOtpGrantParameterNames.CODE),
                singleValue(request, OAuth2ParameterNames.SCOPE),
                singleValue(request, MobileOtpGrantParameterNames.DEVICE_ID),
                singleValue(request, MobileOtpGrantParameterNames.DEVICE_TYPE),
                singleValue(request, MobileOtpGrantParameterNames.DEVICE_NAME),
                singleValue(request, MobileOtpGrantParameterNames.CLIENT_APP_ID),
                singleValue(
                        request,
                        MobileOtpGrantParameterNames.CLIENT_PLATFORM),
                singleValue(
                        request,
                        MobileOtpGrantParameterNames.CLIENT_VERSION),
                singleValue(
                        request,
                        MobileOtpGrantParameterNames.CHANNEL_CODE));
        validate(grantRequest);
        return grantRequest;
    }

    private static Long parseChallengeId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw oauth2Exception(
                    "OAuth2 parameter must be a valid integer: "
                            + MobileOtpGrantParameterNames.CHALLENGE_ID);
        }
    }

    private void validate(MobileOtpGrantRequest request) {
        Set<ConstraintViolation<MobileOtpGrantRequest>> violations =
                validator.validate(request);
        if (!CollectionUtils.isEmpty(violations)) {
            ConstraintViolation<MobileOtpGrantRequest> violation =
                    violations.iterator().next();
            throw oauth2Exception(
                    "OAuth2 parameter is invalid: "
                            + violation.getPropertyPath());
        }
    }

    private static String singleValue(
            HttpServletRequest request,
            String name
    ) {
        String[] values = request.getParameterValues(name);
        if (values == null) {
            return null;
        }
        if (values.length != 1) {
            throw oauth2Exception(
                    "OAuth2 parameter must occur at most once: " + name);
        }
        return StringUtils.hasText(values[0]) ? values[0] : null;
    }

    private static OAuth2AuthenticationException oauth2Exception(
            String description
    ) {
        OAuth2Error error = new OAuth2Error(
                OAuth2ErrorCodes.INVALID_REQUEST,
                description,
                null);
        return new OAuth2AuthenticationException(error);
    }
}
