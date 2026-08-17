package com.cloud.userauth.infrastructure.oauth2.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.authentication.session.SessionStatus;
import com.cloud.userauth.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

class RedisOAuth2AuthorizationServiceTest {
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");

    @Test
    void shouldRevokeTokenFamilyAndSessionWhenRotatedRefreshTokenIsReused() {
        Authorizations authorizations = new Authorizations();
        Sessions sessions = new Sessions();
        SessionId sessionId = new SessionId("session-1");
        sessions.save(LoginSession.create(
                sessionId,
                new UserId(1001L),
                new AuthAccountId(2001L),
                new CredentialId(3001L),
                LoginScene.MOBILE_LOGIN,
                new Device("device-1", "PHONE", "phone"),
                new Client("app", "IOS", "1.0"),
                NOW,
                NOW.plusSeconds(3600)));
        RedisOAuth2AuthorizationService service = new RedisOAuth2AuthorizationService(
                authorizations,
                sessions,
                Clock.fixed(NOW.plusSeconds(10), ZoneOffset.UTC));

        service.save(authorization(sessionId.value(), "access-2", "refresh-2"));
        authorizations.refreshHistory.put(
                DigestUtils.sha256Hex("refresh-1"),
                sessionId.value());

        assertNull(service.findByToken("refresh-1", OAuth2TokenType.REFRESH_TOKEN));

        OAuth2Authorization revoked = authorizations.findById(sessionId.value());
        assertFalse(revoked.getAccessToken().isActive());
        assertFalse(revoked.getRefreshToken().isActive());
        assertEquals(SessionStatus.REVOKED, sessions.findById(sessionId).orElseThrow().getStatus());
        assertEquals(
                NOW.plusSeconds(10),
                sessions.findById(sessionId).orElseThrow().getLastActiveAt());
    }

    private OAuth2Authorization authorization(
            String sessionId,
            String accessTokenValue,
            String refreshTokenValue
    ) {
        RegisteredClient client = RegisteredClient.withId("client-record-id")
                .clientId("client-id")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(new AuthorizationGrantType(
                        "urn:ietf:params:oauth:grant-type:mobile_otp"))
                .scope("user.read")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue,
                NOW,
                NOW.plusSeconds(300),
                Set.of("user.read"));
        OAuth2RefreshToken refreshToken =
                new OAuth2RefreshToken(refreshTokenValue, NOW, NOW.plusSeconds(600));
        return OAuth2Authorization.withRegisteredClient(client)
                .id(sessionId)
                .principalName("1001")
                .authorizationGrantType(client.getAuthorizationGrantTypes().iterator().next())
                .authorizedScopes(Set.of("user.read"))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private static final class Authorizations implements OAuth2AuthorizationRedisStore {
        private final Map<String, OAuth2Authorization> values = new HashMap<>();
        private final Map<String, String> refreshHistory = new HashMap<>();

        @Override
        public void save(OAuth2Authorization authorization) {
            values.put(authorization.getId(), authorization);
        }

        @Override
        public void remove(OAuth2Authorization authorization) {
            values.remove(authorization.getId());
        }

        @Override
        public OAuth2Authorization findById(String authorizationId) {
            return values.get(authorizationId);
        }

        @Override
        public OAuth2Authorization findByTokenHash(
                String tokenHash,
                OAuth2TokenType tokenType
        ) {
            return values.values().stream()
                    .filter(authorization -> matches(authorization, tokenHash, tokenType))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Optional<String> findReusedRefreshTokenAuthorizationId(
                String refreshTokenHash
        ) {
            return Optional.ofNullable(refreshHistory.get(refreshTokenHash));
        }

        private boolean matches(
                OAuth2Authorization authorization,
                String tokenHash,
                OAuth2TokenType tokenType
        ) {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
                return authorization.getAccessToken()
                        .getToken()
                        .getTokenValue()
                        .equals(tokenHash);
            }
            return authorization.getRefreshToken()
                    .getToken()
                    .getTokenValue()
                    .equals(tokenHash);
        }
    }

    private static final class Sessions implements LoginSessionRepository {
        private final Map<String, LoginSession> values = new HashMap<>();

        @Override
        public SessionId nextId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save(LoginSession value) {
            values.put(value.id().value(), value);
        }

        @Override
        public Optional<LoginSession> findById(SessionId id) {
            return Optional.ofNullable(values.get(id.value()));
        }

        @Override
        public List<LoginSession> findActiveByUserId(UserId userId) {
            return List.of();
        }

        @Override
        public List<LoginSession> findActiveByUserIdAndDevice(
                UserId userId,
                String deviceId
        ) {
            return List.of();
        }
    }
}
