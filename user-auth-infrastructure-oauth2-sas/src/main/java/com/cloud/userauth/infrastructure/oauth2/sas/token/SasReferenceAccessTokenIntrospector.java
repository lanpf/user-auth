package com.cloud.userauth.infrastructure.oauth2.sas.token;

import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/** 使用本地 SAS 授权存储校验 Reference Access Token，避免服务内部回环 HTTP 调用。 */
public final class SasReferenceAccessTokenIntrospector implements OpaqueTokenIntrospector {
    private final OAuth2AuthorizationService authorizationService;

    public SasReferenceAccessTokenIntrospector(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2Authorization authorization = authorizationService.findByToken(
                token, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null
                || authorization.getAccessToken() == null
                || !authorization.getAccessToken().isActive()) {
            throw new BadOpaqueTokenException("Access token is inactive");
        }

        Map<String, Object> attributes = new LinkedHashMap<>(
                authorization.getAccessToken().getClaims());
        attributes.putIfAbsent(
                OAuth2TokenIntrospectionClaimNames.SUB, authorization.getPrincipalName());
        attributes.putIfAbsent(
                OAuth2TokenIntrospectionClaimNames.IAT,
                authorization.getAccessToken().getToken().getIssuedAt());
        attributes.putIfAbsent(
                OAuth2TokenIntrospectionClaimNames.EXP,
                authorization.getAccessToken().getToken().getExpiresAt());
        attributes.putIfAbsent(
                OAuth2TokenIntrospectionClaimNames.SCOPE,
                authorization.getAccessToken().getToken().getScopes());
        requireAttribute(attributes, AccessTokenClaimApiConstants.USER_ID_CLAIM);
        requireAttribute(attributes, AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM);
        requireAttribute(attributes, AccessTokenClaimApiConstants.SESSION_ID_CLAIM);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorization.getAccessToken().getToken().getScopes().stream()
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .forEach(authorities::add);
        return new DefaultOAuth2AuthenticatedPrincipal(
                authorization.getPrincipalName(), attributes, authorities);
    }

    private static void requireAttribute(Map<String, Object> attributes, String name) {
        if (attributes.get(name) == null) {
            throw new BadOpaqueTokenException("Required access token claim is missing: " + name);
        }
    }
}
