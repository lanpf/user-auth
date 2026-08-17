package com.cloud.userauth.infrastructure.oauth2.sas.login;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.login.refresh.RefreshLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshLoginCommandOutput;
import com.cloud.userauth.application.port.RefreshTokenRotationLock;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasRefreshTokenEndpointPayload;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointClient;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;
import com.cloud.userauth.infrastructure.oauth2.sas.protocol.SasAuthorizationAttributes;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SasLoginTokenRefresherTest {
    @Test
    void shouldPreserveLoginIdentityAndReturnRotatedTokens() {
        OAuth2Authorization authorization = authorization("app");
        SasLoginTokenRefresher refresher = new SasLoginTokenRefresher(
                new StubAuthorizationService(authorization),
                new StubTokenEndpointClient(),
                new StubLoginSessionRepository(loginSession("app")),
                Clock.fixed(Instant.parse("2026-08-10T00:01:00Z"), ZoneOffset.UTC),
                RefreshTokenRotationLock.direct());

        RefreshLoginCommandOutput output =
                refresher.refresh(new RefreshLoginCommand("app", "refresh-1"));

        assertEquals("access-2", output.accessToken());
        assertEquals("refresh-2", output.refreshToken());
        assertEquals(1001L, output.userId());
        assertEquals(2001L, output.authAccountId());
        assertEquals("session-1", output.sessionId());
    }

    @Test
    void shouldRejectRefreshTokenIssuedForAnotherClientApp() {
        SasLoginTokenRefresher refresher = new SasLoginTokenRefresher(
                new StubAuthorizationService(authorization("app-a")),
                new StubTokenEndpointClient(),
                new StubLoginSessionRepository(loginSession("app-a")),
                Clock.fixed(Instant.parse("2026-08-10T00:01:00Z"), ZoneOffset.UTC),
                RefreshTokenRotationLock.direct());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> refresher.refresh(new RefreshLoginCommand("app-b", "refresh-1")));

        assertEquals(ApplicationError.APP_REFRESH_TOKEN_FAILED.errorCode(),
                exception.getErrorCode());
    }

    private static OAuth2Authorization authorization(String clientAppId) {
        RegisteredClient client = RegisteredClient.withId("internal-client")
                .clientId("user-auth-internal")
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();
        Instant now = Instant.parse("2026-08-10T00:00:00Z");
        return OAuth2Authorization.withRegisteredClient(client)
                .id("session-1")
                .principalName("1001")
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .attribute(SasAuthorizationAttributes.CLIENT_APP_ID, clientAppId)
                .attribute(SasAuthorizationAttributes.USER_ID, 1001L)
                .attribute(SasAuthorizationAttributes.AUTH_ACCOUNT_ID, 2001L)
                .attribute(SasAuthorizationAttributes.SESSION_ID, "session-1")
                .refreshToken(new OAuth2RefreshToken(
                        "refresh-1", now, now.plusSeconds(3600)))
                .build();
    }

    private static LoginSession loginSession(String clientAppId) {
        Instant now = Instant.parse("2026-08-10T00:00:00Z");
        return LoginSession.create(
                new SessionId("session-1"),
                new UserId(1001L),
                new AuthAccountId(2001L),
                new CredentialId(3001L),
                LoginScene.MOBILE_LOGIN,
                new Device("device-1", "PHONE", "phone"),
                new Client(clientAppId, "IOS", "1.0"),
                now,
                now.plusSeconds(3600));
    }

    private record StubAuthorizationService(OAuth2Authorization authorization)
            implements OAuth2AuthorizationService {
        @Override
        public void save(OAuth2Authorization authorization) {
        }

        @Override
        public void remove(OAuth2Authorization authorization) {
        }

        @Override
        public OAuth2Authorization findById(String id) {
            return authorization;
        }

        @Override
        public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
            return authorization;
        }
    }

    private static final class StubTokenEndpointClient implements SasTokenEndpointClient {
        @Override
        public SasTokenEndpointPayload requestToken(MobileOtpGrantRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SasTokenEndpointPayload requestExternalToken(ExternalIdentityGrantRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SasRefreshTokenEndpointPayload requestRefreshToken(String refreshToken) {
            return new SasRefreshTokenEndpointPayload(
                    "Bearer", "access-2", "refresh-2", 900L, "app.api");
        }
    }

    private static final class StubLoginSessionRepository implements LoginSessionRepository {
        private LoginSession session;

        private StubLoginSessionRepository(LoginSession session) {
            this.session = session;
        }

        @Override
        public SessionId nextId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save(LoginSession value) {
            session = value;
        }

        @Override
        public Optional<LoginSession> findById(SessionId id) {
            return Optional.ofNullable(session);
        }

        @Override
        public List<LoginSession> findActiveByUserId(UserId userId) {
            return List.of();
        }

        @Override
        public List<LoginSession> findActiveByUserIdAndDevice(UserId userId, String deviceId) {
            return List.of();
        }
    }
}
