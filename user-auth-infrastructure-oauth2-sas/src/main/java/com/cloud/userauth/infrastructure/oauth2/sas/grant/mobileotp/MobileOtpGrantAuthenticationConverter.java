package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class MobileOtpGrantAuthenticationConverter implements AuthenticationConverter {
    private final MobileOtpGrantRequestParser requestParser;

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!MobileOtpGrantTypes.VALUE.equals(grantType)) {
            return null;
        }
        if (StringUtils.hasText(request.getQueryString())) {
            throw oauth2Exception(
                    "OAuth2 Token request must not contain query parameters");
        }
        validateContentType(request);

        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        return new MobileOtpGrantAuthenticationToken(
                clientPrincipal,
                requestParser.parse(request));
    }

    private static void validateContentType(HttpServletRequest request) {
        try {
            String contentType = request.getContentType();
            if (!StringUtils.hasText(contentType)
                    || !MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(
                            MediaType.parseMediaType(contentType))) {
                throw oauth2Exception(
                        "OAuth2 Token request must use application/x-www-form-urlencoded");
            }
        } catch (InvalidMediaTypeException exception) {
            throw oauth2Exception(
                    "OAuth2 Token request contains an invalid Content-Type");
        }
    }


    private static OAuth2AuthenticationException oauth2Exception(String description) {
        OAuth2Error error = new OAuth2Error(
                OAuth2ErrorCodes.INVALID_REQUEST,
                description,
                null);
        return new OAuth2AuthenticationException(error);
    }
}
