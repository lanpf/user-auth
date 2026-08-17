package com.cloud.userauth;

import com.cloud.framework.core.RequestHeader;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import com.cloud.userauth.application.port.H5SessionStore;
import com.cloud.userauth.application.port.LoginTokenIssuer;
import com.cloud.userauth.application.port.LoginTokenRefresher;
import com.cloud.userauth.application.port.SessionAuthorizationRevoker;
import com.cloud.userauth.application.port.SessionHandoffTicketStore;
import com.cloud.userauth.application.port.SessionTokenStore;
import com.cloud.userauth.application.port.UserGateway;
import com.cloud.userauth.application.session.handoff.SessionHandoffTarget;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.credential.CredentialId;
import com.cloud.userauth.domain.authentication.session.Client;
import com.cloud.userauth.domain.authentication.session.Device;
import com.cloud.userauth.domain.authentication.session.LoginScene;
import com.cloud.userauth.domain.authentication.session.LoginSession;
import com.cloud.userauth.domain.authentication.session.LoginSessionRepository;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.session.redis.RedisH5SessionStore;
import com.cloud.userauth.infrastructure.session.redis.RedisSessionTokenStore;
import com.cloud.userauth.infrastructure.session.redis.login.SessionTokenLoginTokenIssuer;
import com.cloud.userauth.infrastructure.session.redis.login.SessionTokenLoginTokenRefresher;
import com.cloud.userauth.interfaces.security.H5SessionAuthenticationFilter;
import com.cloud.userauth.interfaces.security.SessionTokenAuthenticationFilter;
import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
class SessionTokenOnlyRuntimeIT {
    @Container
    private static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.4.10"))
                    .withDatabaseName("user_auth")
                    .withUsername("user_auth")
                    .withPassword("user_auth");

    @Container
    private static final RedisContainer REDIS =
            new RedisContainer(DockerImageName.parse("redis:8.4.4"));

    private ConfigurableApplicationContext context;

    @AfterEach
    void closeApplication() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void shouldStartWithSessionTokenAndWithoutAuthorizationServer() {
        context = startApplication();

        assertThat(context.getBean(LoginTokenIssuer.class))
                .isInstanceOf(SessionTokenLoginTokenIssuer.class);
        assertThat(context.getBean(LoginTokenRefresher.class))
                .isInstanceOf(SessionTokenLoginTokenRefresher.class);
        assertThat(context.getBean(ClientRenewalPolicyResolver.class).resolve("app"))
                .isEqualTo(ClientRenewalPolicy.NONE);
        assertThat(context.getBean(ClientRenewalPolicyResolver.class).resolve("mini-program"))
                .isEqualTo(ClientRenewalPolicy.EXTERNAL_AUTHORIZATION_CODE);
        assertThat(context.getBean(SessionTokenStore.class))
                .isInstanceOf(RedisSessionTokenStore.class);
        assertThat(context.getBean(H5SessionStore.class))
                .isInstanceOf(RedisH5SessionStore.class);
        assertThat(context.getBeansOfType(SessionTokenAuthenticationFilter.class)).hasSize(1);
        assertThat(context.getBeansOfType(H5SessionAuthenticationFilter.class)).hasSize(1);
        assertThat(context.getBeansOfType(SessionAuthorizationRevoker.class)).isEmpty();
        assertThat(context.getBeansOfType(AuthorizationServerSettings.class)).isEmpty();
        assertThat(context.getBeansOfType(RegisteredClientRepository.class)).isEmpty();
        assertThat(context.getBeansOfType(OAuth2AuthorizationService.class)).isEmpty();
        assertThat(context.getBeansOfType(JwtDecoder.class)).isEmpty();
        assertThat(context.getBeansOfType(SecurityFilterChain.class)).hasSize(1);

        verifyHostSessionWriteBoundary();

        SessionTokenOnlyRuntimeObservation observation = observation();
        manualInspectionBreakpoint(observation);
        log.info("Session-Token-only runtime observation: {}", observation);
    }

    private void verifyHostSessionWriteBoundary() {
        SessionId sessionId = new SessionId("session-token-only-host-boundary");
        Instant now = Instant.now();
        insertAuthenticationPrerequisites(now);
        context.getBean(LoginSessionRepository.class).save(LoginSession.create(
                sessionId,
                new UserId(1001L),
                new AuthAccountId(2001L),
                new CredentialId(3001L),
                LoginScene.MOBILE_LOGIN,
                new Device("device-1", "PHONE", "phone"),
                new Client("app", "MINI_PROGRAM", "1.0"),
                now,
                now.plus(Duration.ofHours(1))));
        AuthenticatedSession authenticatedSession =
                new AuthenticatedSession(1001L, 2001L, sessionId);
        String sessionToken = context.getBean(SessionTokenStore.class)
                .create(authenticatedSession, Duration.ofMinutes(30))
                .token();
        String h5Credential = context.getBean(H5SessionStore.class)
                .create(authenticatedSession, Duration.ofMinutes(30), Duration.ofHours(1))
                .credential();
        SessionHandoffTicketStore.IssuedTicket issuedTicket =
                context.getBean(SessionHandoffTicketStore.class).issue(
                        authenticatedSession,
                        "logout-cascade-handoff",
                        SessionHandoffTarget.H5_SESSION,
                        Duration.ofMinutes(5));

        int port = ((WebServerApplicationContext) context).getWebServer().getPort();
        HttpClient client = HttpClient.newHttpClient();
        assertThat(send(client, port, "/api/user-auth/web-view-handoffs",
                "X-Session-Token", sessionToken).statusCode()).isEqualTo(200);
        assertThat(send(client, port, "/api/user-auth/web-view-handoffs",
                "Cookie", "H5_SESSION=" + h5Credential).statusCode()).isEqualTo(403);
        assertThat(send(client, port, "/api/user-auth/credentials/external/bind",
                "Cookie", "H5_SESSION=" + h5Credential).statusCode()).isEqualTo(403);
        assertThat(send(client, port, "/api/user-auth/logout",
                "Cookie", "H5_SESSION=" + h5Credential).statusCode()).isEqualTo(403);
        assertThat(send(client, port, "/api/user-auth/logout",
                "X-Session-Token", sessionToken).statusCode()).isEqualTo(200);

        assertThat(context.getBean(SessionTokenStore.class)
                .findAuthenticatedSession(sessionToken)).isEmpty();
        assertThat(context.getBean(H5SessionStore.class).find(h5Credential)).isEmpty();
        assertThat(context.getBean(SessionHandoffTicketStore.class)
                .consume(issuedTicket.ticket())).isEmpty();
        assertSessionArtifactsDeleted(
                sessionId, sessionToken, h5Credential, issuedTicket.ticket());
    }

    private void assertSessionArtifactsDeleted(
            SessionId sessionId,
            String sessionToken,
            String h5Credential,
            String handoffTicket
    ) {
        StringRedisTemplate redis = context.getBean(StringRedisTemplate.class);
        assertThat(redis.hasKey("user-auth:session-token:" + sessionToken)).isFalse();
        assertThat(redis.hasKey("user-auth:session-token:login-session:" + sessionId.value())).isFalse();
        assertThat(redis.hasKey("user-auth:h5-session:" + h5Credential)).isFalse();
        assertThat(redis.hasKey("user-auth:h5-session:parent:" + sessionId.value())).isFalse();
        assertThat(redis.hasKey("user-auth:session-handoff:ticket:" + handoffTicket)).isFalse();
        assertThat(redis.hasKey(
                "user-auth:session-handoff:login-session:" + sessionId.value())).isFalse();
    }

    private void insertAuthenticationPrerequisites(Instant now) {
        JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
        Timestamp timestamp = Timestamp.from(now);
        jdbc.update(
                """
                        INSERT INTO ua_auth_account
                            (id, user_id, status, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                2001L, 1001L, "ACTIVE", timestamp, timestamp);
        jdbc.update(
                """
                        INSERT INTO ua_credential
                            (id, auth_account_id, credential_type, issuer, issuer_type,
                             principal, status, verified_at, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                3001L, 2001L, "MOBILE", "LOCAL", "LOCAL",
                "13800138000", "ACTIVE", timestamp, timestamp, timestamp);
    }

    private static HttpResponse<String> send(
            HttpClient client,
            int port,
            String path,
            String credentialHeader,
            String credential
    ) {
        try {
            return client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                            .header(credentialHeader, credential)
                            .header(RequestHeader.CLIENT_APP_ID, "app")
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{}"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private ConfigurableApplicationContext startApplication() {
        String datasourceUrl = MYSQL.getJdbcUrl()
                + "?preserveInstants=true&connectionTimeZone=UTC"
                + "&forceConnectionTimeZoneToSession=true";
        return new SpringApplicationBuilder(
                UserAuthApplication.class,
                IntegrationTestConfiguration.class)
                .run(
                        "--server.port=0",
                        "--spring.config.additional-location=optional:file:../config/",
                        "--USER_AUTH_CONFIG_DIR=../config",
                        "--spring.datasource.url=" + datasourceUrl,
                        "--spring.datasource.username=" + MYSQL.getUsername(),
                        "--spring.datasource.password=" + MYSQL.getPassword(),
                        "--spring.data.redis.host=" + REDIS.getHost(),
                        "--spring.data.redis.port=" + REDIS.getRedisPort(),
                        "--user-auth.authentication.access-token.provider=session-token",
                        "--user-auth.authentication.client-apps.app.renewal-policy=NONE",
                        "--user-auth.authentication.oauth2.authorization-server.enabled=false");
    }

    private SessionTokenOnlyRuntimeObservation observation() {
        int port = ((WebServerApplicationContext) context).getWebServer().getPort();
        Map<String, SecurityFilterChain> securityChains = context.getBeansOfType(SecurityFilterChain.class);
        return new SessionTokenOnlyRuntimeObservation(
                port,
                context.getBean(LoginTokenIssuer.class).getClass().getSimpleName(),
                context.getBean(LoginTokenRefresher.class).getClass().getSimpleName(),
                securityChains.keySet(),
                java.util.Set.of(
                        context.getBean(SessionTokenAuthenticationFilter.class).getClass().getSimpleName(),
                        context.getBean(H5SessionAuthenticationFilter.class).getClass().getSimpleName()),
                context.getBeansOfType(JwtDecoder.class).size(),
                context.getBeansOfType(AuthorizationServerSettings.class).size());
    }

    private static void manualInspectionBreakpoint(SessionTokenOnlyRuntimeObservation observation) {
        // IDE 调试时在此处设置断点，可观察 Session-Token-only 的实际 Bean 装配和随机 HTTP 端口。
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class IntegrationTestConfiguration {
        @Bean
        UserGateway integrationTestUserGateway() {
            return userId -> { };
        }
    }

    private record SessionTokenOnlyRuntimeObservation(
            int serverPort,
            String loginTokenIssuer,
            String loginTokenRefresher,
            java.util.Set<String> securityFilterChains,
            java.util.Set<String> sessionAuthenticationFilters,
            int jwtDecoderCount,
            int authorizationServerSettingsCount
    ) {
    }
}
