package com.cloud.userauth;

import com.cloud.framework.core.RequestHeader;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.enums.AuthChallengeSceneApiEnum;
import com.cloud.userauth.api.enums.AuthChallengeTypeApiEnum;
import com.cloud.userauth.application.port.AuthChallengeDispatcher;
import com.cloud.userauth.application.port.MobileOtpLoginLock;
import com.cloud.userauth.application.port.UserGateway;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.user.UserId;
import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
class MobileOtpLoginFlowIT {
    private static final String MOBILE = "13800138000";
    private static final String CLIENT_APP_ID = "app";
    private static final String OAUTH2_NAMESPACE = "user-auth-it";
    private static final String LOCK_NAMESPACE = "user-auth-lock-it";
    private static final String SESSION_HANDOFF_NAMESPACE = "user-auth-session-it";
    private static final String OAUTH2_RESOURCE_PREFIX = "oauth2:{authorization-state}";

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
    void shouldIssueMobileCodeAndLoginUsingMySqlAndRedis() throws Exception {
        int serverPort = availablePort();
        context = startApplication(serverPort, "SELF_CONTAINED");
        RestClient restClient = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .build();

        IssueAuthChallengeApiCommandOutput challenge = issueChallenge(restClient);
        ChallengeProbe challengeProbe = context.getBean(ChallengeProbe.class);
        DispatchedChallenge dispatched = challengeProbe.last();

        assertThat(challenge.reused()).isFalse();
        assertThat(dispatched).isNotNull();
        assertThat(dispatched.type()).isEqualTo(AuthChallengeType.SMS_OTP);
        assertThat(dispatched.target()).isEqualTo(MOBILE);
        log.info(
                "Mobile challenge observed: challengeId={}, type={}, target={}, expiresAt={}",
                challenge.challengeId(),
                dispatched.type(),
                dispatched.target(),
                challenge.expiresAt());

        MobileOtpLoginApiCommandOutput login =
                login(restClient, challenge.challengeId(), dispatched.code());

        assertThat(login.tokenType()).isEqualToIgnoringCase("Bearer");
        assertThat(login.accessToken()).isNotBlank();
        assertThat(login.refreshToken()).isNotBlank();
        assertThat(login.scope()).isEqualTo("app");
        assertThat(login.fromRegistrationFlow()).isTrue();
        assertThat(login.replayed()).isFalse();

        DatabaseObservation database = observeDatabase(challenge.challengeId(), login);
        RedisObservation redis = observeRedis(login);
        UserProbe userProbe = context.getBean(UserProbe.class);
        ManualInspection manualInspection =
                manualInspection(challenge.challengeId(), login, redis);

        manualInspectionBreakpoint(manualInspection);

        assertThat(userProbe.initializedUserId()).isEqualTo(login.userId());
        assertDatabaseState(database, challenge.challengeId(), login);
        assertRedisState(redis, login);
        assertLockNamespaceInjectedOnce();

        log.info(
                "MySQL business observation: challenge={}, account={}, credential={}, "
                        + "registration={}, session={}, registeredClient={}, domainEventCount={}",
                database.challenge(),
                database.account(),
                database.credential(),
                database.registration(),
                database.session(),
                database.registeredClient(),
                database.domainEventCount());
        log.info(
                "Redis token observation: authorizationKey={}, accessTokenHash={}, "
                        + "accessIndexKey={}, authorizationTtlSeconds={}, accessIndexTtlSeconds={}, "
                        + "storedAuthorizationBytes={}, "
                        + "plaintextTokenStored={}",
                redis.authorizationKey(),
                redis.accessTokenHash(),
                redis.accessIndexKey(),
                redis.authorizationTtlSeconds(),
                redis.accessIndexTtlSeconds(),
                redis.storedAuthorizationBytes(),
                redis.plaintextTokenStored());

        context.close();
        context = null;
        verifyReferenceAccessTokenFlow();
    }

    private void verifyReferenceAccessTokenFlow() throws Exception {
        int serverPort = availablePort();
        context = startApplication(serverPort, "REFERENCE");
        RestClient restClient = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + serverPort)
                .build();
        IssueAuthChallengeApiCommandOutput challenge = issueChallenge(restClient);
        String code = context.getBean(ChallengeProbe.class).last().code();

        MobileOtpLoginApiCommandOutput login = login(restClient, challenge.challengeId(), code);

        assertThat(login.tokenType()).isEqualToIgnoringCase("Bearer");
        assertThat(login.accessToken()).isNotBlank().doesNotContain(".");
        assertThat(context.getBeansOfType(JwtDecoder.class)).isEmpty();
        assertThat(introspect(restClient, login.accessToken()).get("active")).isEqualTo(true);

        Result<CreateSessionHandoffApiCommandOutput> handoff = restClient.post()
                .uri("/api/user-auth/handoffs")
                .header(RequestHeader.CLIENT_APP_ID, CLIENT_APP_ID)
                .header(RequestHeader.USER_ID, login.userId().toString())
                .header(RequestHeader.SESSION_ID, login.sessionId())
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"target\":\"BROWSER_SESSION\"}")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assertThat(handoff).isNotNull();
        assertThat(handoff.isSuccess()).isTrue();
        assertThat(handoff.getData()).isNotNull();
        assertThat(handoff.getData().ticket()).isNotBlank();
        assertSessionHandoffNamespaceInjectedOnce(login, handoff.getData());

        restClient.post()
                .uri("/api/user-auth/logout")
                .header(RequestHeader.CLIENT_APP_ID, CLIENT_APP_ID)
                .header(RequestHeader.USER_ID, login.userId().toString())
                .header(RequestHeader.SESSION_ID, login.sessionId())
                .retrieve()
                .toBodilessEntity();
        assertThat(introspect(restClient, login.accessToken()).get("active")).isEqualTo(false);
        log.info(
                "Reference access token observed: sessionId={}, tokenLength={}, handoffId={}",
                login.sessionId(),
                login.accessToken().length(),
                handoff.getData().handoffId());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> introspect(RestClient restClient, String accessToken) {
        return restClient.post()
                .uri("/oauth2/introspect")
                .headers(headers -> headers.setBasicAuth(
                        "gateway-introspection-client", "local-gateway-introspection-secret"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("token=" + accessToken)
                .retrieve()
                .body(Map.class);
    }

    private static ManualInspection manualInspection(
            Long challengeId,
            MobileOtpLoginApiCommandOutput login,
            RedisObservation redis
    ) {
        return new ManualInspection(
                MYSQL.getContainerId(),
                MYSQL.getJdbcUrl(),
                MYSQL.getUsername(),
                MYSQL.getPassword(),
                MYSQL.getDatabaseName(),
                REDIS.getContainerId(),
                REDIS.getHost(),
                REDIS.getRedisPort(),
                OAUTH2_NAMESPACE + ":" + OAUTH2_RESOURCE_PREFIX + ":*",
                challengeId,
                login.userId(),
                login.authAccountId(),
                login.sessionId(),
                redis.authorizationKey(),
                redis.accessIndexKey());
    }

    private static void manualInspectionBreakpoint(ManualInspection inspection) {
        log.info(
                "Manual inspection ready: set a debugger breakpoint in "
                        + "manualInspectionBreakpoint; mysqlContainerId={}, jdbcUrl={}, "
                        + "mysqlUsername={}, mysqlDatabase={}, redisContainerId={}, "
                        + "redisHost={}, redisPort={}, redisKeyPattern={}",
                inspection.mysqlContainerId(),
                inspection.mysqlJdbcUrl(),
                inspection.mysqlUsername(),
                inspection.mysqlDatabase(),
                inspection.redisContainerId(),
                inspection.redisHost(),
                inspection.redisPort(),
                inspection.redisKeyPattern());
    }

    private ConfigurableApplicationContext startApplication(
            int serverPort,
            String accessTokenFormat
    ) {
        String tokenEndpoint = "http://127.0.0.1:" + serverPort + "/oauth2/token";
        String datasourceUrl = MYSQL.getJdbcUrl()
                + "?preserveInstants=true&connectionTimeZone=UTC"
                + "&forceConnectionTimeZoneToSession=true";
        return new SpringApplicationBuilder(
                UserAuthApplication.class,
                IntegrationTestConfiguration.class)
                .run(
                        "--server.port=" + serverPort,
                        "--spring.config.additional-location=optional:file:../config/",
                        "--USER_AUTH_CONFIG_DIR=../config",
                        "--spring.datasource.url=" + datasourceUrl,
                        "--spring.datasource.username=" + MYSQL.getUsername(),
                        "--spring.datasource.password=" + MYSQL.getPassword(),
                        "--spring.data.redis.host=" + REDIS.getHost(),
                        "--spring.data.redis.port=" + REDIS.getRedisPort(),
                        "--user-auth.authentication.oauth2.access-token.format="
                                + accessTokenFormat,
                        "--user-auth.authentication.oauth2.authorization-store.namespace=" + OAUTH2_NAMESPACE,
                        "--user-auth.authentication.session-handoff.namespace=" + SESSION_HANDOFF_NAMESPACE,
                        "--framework.lock.redis.namespace=" + LOCK_NAMESPACE,
                        "--user-auth.authentication.oauth2.authorization-server.sas.issuer=http://127.0.0.1:"
                                + serverPort,
                        "--user-auth.authentication.oauth2.authorization-server.sas.internal-token-client.token-endpoint="
                                + tokenEndpoint);
    }

    private static IssueAuthChallengeApiCommandOutput issueChallenge(RestClient restClient) {
        Result<IssueAuthChallengeApiCommandOutput> result = restClient.post()
                .uri("/api/user-auth/challenges")
                .header(RequestHeader.CLIENT_APP_ID, CLIENT_APP_ID)
                .header(RequestHeader.CLIENT_PLATFORM, "IOS")
                .header(RequestHeader.CLIENT_VERSION, "1.0.0")
                .header(RequestHeader.CHANNEL_CODE, "DIRECT")
                .body(Map.of(
                        "challengeType", AuthChallengeTypeApiEnum.SMS_OTP,
                        "target", MOBILE,
                        "scene", AuthChallengeSceneApiEnum.LOGIN))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        return result.getData();
    }

    private static MobileOtpLoginApiCommandOutput login(
            RestClient restClient,
            Long challengeId,
            String code
    ) {
        Result<MobileOtpLoginApiCommandOutput> result = restClient.post()
                .uri("/api/user-auth/login/mobile-otp")
                .header(RequestHeader.CLIENT_APP_ID, CLIENT_APP_ID)
                .header(RequestHeader.CLIENT_PLATFORM, "IOS")
                .header(RequestHeader.CLIENT_VERSION, "1.0.0")
                .header(RequestHeader.CHANNEL_CODE, "DIRECT")
                .body(new MobileOtpLoginApiCommand(
                        challengeId,
                        code,
                        CLIENT_APP_ID,
                        "IOS",
                        "1.0.0",
                        "DIRECT",
                        "device-1",
                        "PHONE",
                        "Integration test phone"))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        return result.getData();
    }

    private DatabaseObservation observeDatabase(
            Long challengeId,
            MobileOtpLoginApiCommandOutput login
    ) {
        JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
        return new DatabaseObservation(
                jdbc.queryForMap(
                        """
                                SELECT id, challenge_type, target, scene, status, attempts,
                                       consumed_by_type, consumed_by_id, expires_at
                                FROM ua_auth_challenge
                                WHERE id = ?
                                """,
                        challengeId),
                jdbc.queryForMap(
                        """
                                SELECT id, user_id, status, created_at
                                FROM ua_auth_account
                                WHERE id = ?
                                """,
                        login.authAccountId()),
                jdbc.queryForMap(
                        """
                                SELECT id, auth_account_id, credential_type, issuer, issuer_type,
                                       principal, status, verified_at
                                FROM ua_credential
                                WHERE auth_account_id = ?
                                """,
                        login.authAccountId()),
                jdbc.queryForMap(
                        """
                                SELECT id, challenge_id, user_id, auth_account_id, status,
                                       retry_count, session_id
                                FROM ua_registration_process
                                WHERE challenge_id = ?
                                """,
                        challengeId),
                jdbc.queryForMap(
                        """
                                SELECT id, user_id, auth_account_id, authenticated_credential_id,
                                       status, login_scene, device_id, client_app_id,
                                       issued_at, expires_at
                                FROM ua_login_session
                                WHERE id = ?
                                """,
                        login.sessionId()),
                jdbc.queryForMap(
                        """
                                SELECT id, client_id, client_name, authorization_grant_types, scopes
                                FROM oauth2_registered_client
                                WHERE client_id = ?
                                """,
                        "user-auth-client"),
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM ua_domain_event",
                        Long.class));
    }

    private RedisObservation observeRedis(MobileOtpLoginApiCommandOutput login) {
        StringRedisTemplate redis = context.getBean(StringRedisTemplate.class);
        String accessTokenHash = sha256(login.accessToken());
        String refreshTokenHash = sha256(login.refreshToken());
        String prefix = OAUTH2_NAMESPACE + ":" + OAUTH2_RESOURCE_PREFIX + ":";
        String authorizationKey = prefix + "authorization:" + login.sessionId();
        String accessIndexKey = prefix + "access-token:" + accessTokenHash;
        String refreshIndexKey = prefix + "refresh-token:" + refreshTokenHash;
        String storedAuthorization = redis.opsForValue().get(authorizationKey);
        Set<String> keys = redis.keys(prefix + "*");
        return new RedisObservation(
                authorizationKey,
                accessTokenHash,
                accessIndexKey,
                redis.opsForValue().get(accessIndexKey),
                refreshIndexKey,
                redis.opsForValue().get(refreshIndexKey),
                redis.getExpire(authorizationKey),
                redis.getExpire(accessIndexKey),
                redis.getExpire(refreshIndexKey),
                storedAuthorization == null
                        ? 0
                        : storedAuthorization.getBytes(StandardCharsets.UTF_8).length,
                storedAuthorization != null
                        && (storedAuthorization.contains(login.accessToken())
                        || storedAuthorization.contains(login.refreshToken())),
                keys == null ? Set.of() : keys);
    }

    private void assertLockNamespaceInjectedOnce() throws Exception {
        StringRedisTemplate redis = context.getBean(StringRedisTemplate.class);
        MobileOtpLoginLock lock = context.getBean(MobileOtpLoginLock.class);
        String expectedKey = LOCK_NAMESPACE + ":mobile-login:" + MOBILE;

        lock.execute(new LoginMobile(MOBILE), () -> {
            assertThat(redis.keys("*mobile-login*"))
                    .containsExactly(expectedKey);
            assertThat(expectedKey).doesNotContain(LOCK_NAMESPACE + ":" + LOCK_NAMESPACE);
            return null;
        });
    }

    private void assertSessionHandoffNamespaceInjectedOnce(
            MobileOtpLoginApiCommandOutput login,
            CreateSessionHandoffApiCommandOutput handoff
    ) {
        StringRedisTemplate redis = context.getBean(StringRedisTemplate.class);
        String ticketKey = SESSION_HANDOFF_NAMESPACE + ":session-handoff:ticket:" + handoff.ticket();
        String loginSessionKey = SESSION_HANDOFF_NAMESPACE
                + ":session-handoff:login-session:" + login.sessionId();

        assertThat(redis.keys("*session-handoff*"))
                .containsExactlyInAnyOrder(ticketKey, loginSessionKey);
        assertThat(ticketKey)
                .doesNotContain(SESSION_HANDOFF_NAMESPACE + ":" + SESSION_HANDOFF_NAMESPACE);
        assertThat(loginSessionKey)
                .doesNotContain(SESSION_HANDOFF_NAMESPACE + ":" + SESSION_HANDOFF_NAMESPACE);
    }

    private static void assertDatabaseState(
            DatabaseObservation database,
            Long challengeId,
            MobileOtpLoginApiCommandOutput login
    ) {
        assertThat(number(database.challenge(), "id")).isEqualTo(challengeId);
        assertThat(database.challenge().get("status")).isEqualTo("CONSUMED");
        assertThat(database.challenge().get("consumed_by_type"))
                .isEqualTo("REGISTRATION_PROCESS");

        assertThat(number(database.account(), "id")).isEqualTo(login.authAccountId());
        assertThat(number(database.account(), "user_id")).isEqualTo(login.userId());
        assertThat(database.account().get("status")).isEqualTo("ACTIVE");

        assertThat(database.credential().get("credential_type")).isEqualTo("MOBILE");
        assertThat(database.credential().get("principal")).isEqualTo(MOBILE);
        assertThat(database.credential().get("status")).isEqualTo("ACTIVE");

        assertThat(number(database.registration(), "challenge_id")).isEqualTo(challengeId);
        assertThat(database.registration().get("status")).isEqualTo("COMPLETED");
        assertThat(database.registration().get("session_id")).isEqualTo(login.sessionId());

        assertThat(database.session().get("id")).isEqualTo(login.sessionId());
        assertThat(database.session().get("status")).isEqualTo("ACTIVE");
        assertThat(database.session().get("login_scene")).isEqualTo("MOBILE_LOGIN");
        assertThat(database.session().get("client_app_id")).isEqualTo(CLIENT_APP_ID);
        assertThat(sessionTtl(database.session())).isEqualTo(Duration.ofDays(30));

        assertThat(database.registeredClient().get("scopes").toString())
                .contains("app", "admin");
        assertThat(database.registeredClient().get("authorization_grant_types").toString())
                .contains("mobile_otp");
        assertThat(database.domainEventCount()).isPositive();
    }

    private static void assertRedisState(
            RedisObservation redis,
            MobileOtpLoginApiCommandOutput login
    ) {
        assertThat(redis.accessIndexAuthorizationId()).isEqualTo(login.sessionId());
        assertThat(redis.refreshIndexAuthorizationId()).isEqualTo(login.sessionId());
        assertThat(redis.authorizationTtlSeconds()).isPositive();
        assertThat(redis.accessIndexTtlSeconds()).isPositive();
        assertThat(redis.refreshIndexTtlSeconds()).isPositive();
        assertThat(redis.storedAuthorizationBytes()).isPositive();
        assertThat(redis.plaintextTokenStored()).isFalse();
        assertThat(redis.keys()).containsExactlyInAnyOrder(
                redis.authorizationKey(),
                redis.accessIndexKey(),
                redis.refreshIndexKey());
    }

    private static long number(Map<String, Object> row, String column) {
        return ((Number) row.get(column)).longValue();
    }

    private static Duration sessionTtl(Map<String, Object> session) {
        Instant issuedAt = ((LocalDateTime) session.get("issued_at"))
                .toInstant(ZoneOffset.UTC);
        Instant expiresAt = ((LocalDateTime) session.get("expires_at"))
                .toInstant(ZoneOffset.UTC);
        return Duration.between(issuedAt, expiresAt);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static int availablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class IntegrationTestConfiguration {
        @Bean
        ChallengeProbe challengeProbe() {
            return new ChallengeProbe();
        }

        @Bean
        @Primary
        AuthChallengeDispatcher integrationTestAuthChallengeDispatcher(
                ChallengeProbe probe
        ) {
            return probe::record;
        }

        @Bean
        UserProbe userProbe() {
            return new UserProbe();
        }

        @Bean
        @Primary
        UserGateway integrationTestUserGateway(UserProbe probe) {
            return probe::record;
        }
    }

    static final class ChallengeProbe {
        private final AtomicReference<DispatchedChallenge> last = new AtomicReference<>();

        void record(AuthChallengeType type, ChallengeTarget target, String code) {
            last.set(new DispatchedChallenge(type, target.value(), code));
        }

        DispatchedChallenge last() {
            return last.get();
        }
    }

    static final class UserProbe {
        private final AtomicReference<UserId> initializedUserId = new AtomicReference<>();

        void record(UserId userId) {
            initializedUserId.set(userId);
        }

        Long initializedUserId() {
            UserId userId = initializedUserId.get();
            return userId == null ? null : userId.value();
        }
    }

    private record DispatchedChallenge(
            AuthChallengeType type,
            String target,
            String code
    ) {
    }

    private record DatabaseObservation(
            Map<String, Object> challenge,
            Map<String, Object> account,
            Map<String, Object> credential,
            Map<String, Object> registration,
            Map<String, Object> session,
            Map<String, Object> registeredClient,
            Long domainEventCount
    ) {
    }

    private record RedisObservation(
            String authorizationKey,
            String accessTokenHash,
            String accessIndexKey,
            String accessIndexAuthorizationId,
            String refreshIndexKey,
            String refreshIndexAuthorizationId,
            Long authorizationTtlSeconds,
            Long accessIndexTtlSeconds,
            Long refreshIndexTtlSeconds,
            int storedAuthorizationBytes,
            boolean plaintextTokenStored,
            Set<String> keys
    ) {
    }

    private record ManualInspection(
            String mysqlContainerId,
            String mysqlJdbcUrl,
            String mysqlUsername,
            String mysqlPassword,
            String mysqlDatabase,
            String redisContainerId,
            String redisHost,
            int redisPort,
            String redisKeyPattern,
            Long challengeId,
            Long userId,
            Long authAccountId,
            String sessionId,
            String authorizationKey,
            String accessIndexKey
    ) {
    }
}
