package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasOAuth2RequestMessages;
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
public final class ExternalIdentityGrantAuthenticationConverter implements AuthenticationConverter {
    private final ExternalIdentityGrantRequestParser requestParser;

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!ExternalIdentityGrantTypes.VALUE.equals(
                request.getParameter(OAuth2ParameterNames.GRANT_TYPE))) {
            return null;
        }
        if (StringUtils.hasText(request.getQueryString())) {
            throw oauth2(SasOAuth2RequestMessages.QUERY_PARAMETERS_NOT_ALLOWED);
        }
        try {
            String contentType = request.getContentType();
            if (!StringUtils.hasText(contentType)
                    || !MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(
                            MediaType.parseMediaType(contentType))) {
                throw oauth2(SasOAuth2RequestMessages.FORM_URLENCODED_REQUIRED);
            }
        } catch (InvalidMediaTypeException exception) {
            throw oauth2(SasOAuth2RequestMessages.INVALID_CONTENT_TYPE);
        }
        return new ExternalIdentityGrantAuthenticationToken(
                SecurityContextHolder.getContext().getAuthentication(),
                requestParser.parse(request));
    }

    private static OAuth2AuthenticationException oauth2(String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, description, null));
    }
}
