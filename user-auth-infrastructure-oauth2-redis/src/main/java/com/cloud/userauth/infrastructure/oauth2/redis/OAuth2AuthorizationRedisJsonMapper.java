package com.cloud.userauth.infrastructure.oauth2.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class OAuth2AuthorizationRedisJsonMapper {
    private final RegisteredClientRepository registeredClientRepository;
    private final ObjectMapper dataObjectMapper = dataObjectMapper();
    private final ObjectMapper mapObjectMapper = mapObjectMapper();

    public String write(OAuth2Authorization authorization) {
        try {
            return dataObjectMapper.writeValueAsString(toData(authorization));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to serialize OAuth2 authorization", exception);
        }
    }

    public OAuth2Authorization read(String value) {
        try {
            return toAuthorization(dataObjectMapper.readValue(
                    value,
                    OAuth2AuthorizationRedisData.class));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to deserialize OAuth2 authorization", exception);
        }
    }

    private OAuth2AuthorizationRedisData toData(OAuth2Authorization source) {
        Map<String, Object> attributes = new LinkedHashMap<>(source.getAttributes());
        attributes.remove(Principal.class.getName());
        return new OAuth2AuthorizationRedisData(
                source.getId(),
                source.getRegisteredClientId(),
                source.getPrincipalName(),
                source.getAuthorizationGrantType().getValue(),
                source.getAuthorizedScopes(),
                writeMap(attributes),
                source.getAttribute(OAuth2ParameterNames.STATE),
                accessToken(source.getAccessToken()),
                refreshToken(source.getRefreshToken()));
    }

    private OAuth2AuthorizationRedisData.AccessTokenData accessToken(
            OAuth2Authorization.Token<OAuth2AccessToken> tokenHolder
    ) {
        if (tokenHolder == null) {
            return null;
        }
        OAuth2AccessToken token = tokenHolder.getToken();
        return new OAuth2AuthorizationRedisData.AccessTokenData(
                token.getTokenValue(),
                token.getIssuedAt(),
                token.getExpiresAt(),
                writeMap(tokenHolder.getMetadata()),
                token.getTokenType().getValue(),
                token.getScopes());
    }

    private OAuth2AuthorizationRedisData.RefreshTokenData refreshToken(
            OAuth2Authorization.Token<OAuth2RefreshToken> tokenHolder
    ) {
        if (tokenHolder == null) {
            return null;
        }
        OAuth2RefreshToken token = tokenHolder.getToken();
        return new OAuth2AuthorizationRedisData.RefreshTokenData(
                token.getTokenValue(),
                token.getIssuedAt(),
                token.getExpiresAt(),
                writeMap(tokenHolder.getMetadata()));
    }

    private OAuth2Authorization toAuthorization(OAuth2AuthorizationRedisData source) {
        RegisteredClient registeredClient =
                registeredClientRepository.findById(source.registeredClientId());
        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "Registered OAuth2 client was not found: " + source.registeredClientId());
        }
        OAuth2Authorization.Builder builder = OAuth2Authorization
                .withRegisteredClient(registeredClient)
                .id(source.id())
                .principalName(source.principalName())
                .authorizationGrantType(new AuthorizationGrantType(
                        source.authorizationGrantType()))
                .authorizedScopes(source.authorizedScopes())
                .attributes(attributes -> attributes.putAll(readMap(source.attributes())))
                .attribute(
                        Principal.class.getName(),
                        UsernamePasswordAuthenticationToken.authenticated(
                                source.principalName(),
                                null,
                                List.of()));
        if (StringUtils.hasText(source.state())) {
            builder.attribute(OAuth2ParameterNames.STATE, source.state());
        }
        restoreAccessToken(source.accessToken(), builder);
        restoreRefreshToken(source.refreshToken(), builder);
        return builder.build();
    }

    private void restoreAccessToken(
            OAuth2AuthorizationRedisData.AccessTokenData source,
            OAuth2Authorization.Builder builder
    ) {
        if (source == null) {
            return;
        }
        OAuth2AccessToken token = new OAuth2AccessToken(
                accessTokenType(source.tokenType()),
                source.value(),
                source.issuedAt(),
                source.expiresAt(),
                source.scopes());
        builder.token(token, metadata -> metadata.putAll(readMap(source.metadata())));
    }

    private void restoreRefreshToken(
            OAuth2AuthorizationRedisData.RefreshTokenData source,
            OAuth2Authorization.Builder builder
    ) {
        if (source == null) {
            return;
        }
        OAuth2RefreshToken token =
                new OAuth2RefreshToken(source.value(), source.issuedAt(), source.expiresAt());
        builder.token(token, metadata -> metadata.putAll(readMap(source.metadata())));
    }

    private String writeMap(Map<String, Object> value) {
        try {
            return mapObjectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to serialize OAuth2 metadata", exception);
        }
    }

    private Map<String, Object> readMap(String value) {
        if (!StringUtils.hasText(value)) {
            return Map.of();
        }
        try {
            return mapObjectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to deserialize OAuth2 metadata", exception);
        }
    }

    private static OAuth2AccessToken.TokenType accessTokenType(String value) {
        if (OAuth2AccessToken.TokenType.DPOP.getValue().equalsIgnoreCase(value)) {
            return OAuth2AccessToken.TokenType.DPOP;
        }
        return OAuth2AccessToken.TokenType.BEARER;
    }

    private static ObjectMapper dataObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private static ObjectMapper mapObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
