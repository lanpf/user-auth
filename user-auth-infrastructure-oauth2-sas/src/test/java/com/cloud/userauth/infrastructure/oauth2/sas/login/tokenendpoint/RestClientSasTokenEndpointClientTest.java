package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.RestClientSasTokenEndpointClient;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasRefreshTokenEndpointPayload;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointClient;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointJsonMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.springboot3.bulkhead.autoconfigure.BulkheadAutoConfiguration;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestClientSasTokenEndpointClientTest {
    private static final URI TOKEN_ENDPOINT =
            URI.create("http://127.0.0.1:8081/oauth2/token");
    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void shouldCallTokenEndpointWithProtocolFormAndBasicAuthentication() {
        RestClient.Builder builder = restClientBuilder();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();
        server.expect(httpRequest -> {
                    assertEquals(TOKEN_ENDPOINT, httpRequest.getURI());
                    assertEquals(
                            MediaType.APPLICATION_FORM_URLENCODED,
                            httpRequest.getHeaders().getContentType());
                    String authorization = httpRequest.getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION);
                    assertTrue(authorization != null
                            && authorization.startsWith("Basic "));
                    String body = ((MockClientHttpRequest) httpRequest)
                            .getBodyAsString();
                    assertTrue(body.contains(
                            "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type"
                                    + "%3Amobile_otp"));
                    assertTrue(body.contains("scope=app.api"));
                    assertTrue(body.contains("client_app_id=app"));
                })
                .andRespond(httpRequest -> jsonResponse());
        SasTokenEndpointClient client = client(builder.build());

        SasTokenEndpointPayload response =
                client.requestToken(grantRequest());

        assertEquals("access-token", response.accessToken());
        server.verify();
    }

    @Test
    void shouldCallRefreshGrantAndRequireRotatedRefreshToken() {
        RestClient.Builder builder = restClientBuilder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(httpRequest -> {
                    String body = ((MockClientHttpRequest) httpRequest).getBodyAsString();
                    assertTrue(body.contains("grant_type=refresh_token"));
                    assertTrue(body.contains("refresh_token=refresh-token-1"));
                })
                .andRespond(httpRequest -> refreshJsonResponse());

        SasRefreshTokenEndpointPayload response =
                client(builder.build()).requestRefreshToken("refresh-token-1");

        assertEquals("refresh-token-2", response.refreshToken());
        assertEquals("access-token-2", response.accessToken());
        server.verify();
    }

    @Test
    void shouldApplyBulkheadAtHttpClientBoundary() {
        RestClient.Builder builder = restClientBuilder();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();
        CountDownLatch requestEntered = new CountDownLatch(1);
        CountDownLatch releaseResponse = new CountDownLatch(1);
        server.expect(httpRequest -> { })
                .andRespond(httpRequest -> {
                    requestEntered.countDown();
                    await(releaseResponse);
                    return jsonResponse();
                });
        SasTokenEndpointClient target = client(builder.build());

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        AopAutoConfiguration.class,
                        BulkheadAutoConfiguration.class))
                .withPropertyValues(
                        "resilience4j.bulkhead.instances."
                                + RestClientSasTokenEndpointClient.BULKHEAD_NAME
                                + ".maxConcurrentCalls=1",
                        "resilience4j.bulkhead.instances."
                                + RestClientSasTokenEndpointClient.BULKHEAD_NAME
                                + ".maxWaitDuration=0")
                .withBean(SasTokenEndpointClient.class, () -> target)
                .run(context -> {
                    SasTokenEndpointClient client =
                            context.getBean(SasTokenEndpointClient.class);
                    var executor = Executors.newSingleThreadExecutor();
                    var firstRequest = executor.submit(
                            () -> client.requestToken(grantRequest()));
                    try {
                        assertTrue(requestEntered.await(2, TimeUnit.SECONDS));
                        ApplicationException exception = assertThrows(
                                ApplicationException.class,
                                () -> client.requestToken(grantRequest()));
                        assertEquals(
                                ApplicationError.APP_INTERNAL_TOKEN_REQUEST_LIMIT_REACHED.errorCode(),
                                exception.getErrorCode());
                    } finally {
                        releaseResponse.countDown();
                        executor.shutdown();
                    }
                    assertEquals(
                            "access-token",
                            firstRequest.get(2, TimeUnit.SECONDS).accessToken());
                    assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
                    server.verify();
                });
    }

    private static SasTokenEndpointClient client(RestClient restClient) {
        return new RestClientSasTokenEndpointClient(
                restClient,
                TOKEN_ENDPOINT,
                "user-auth-client",
                "secret",
                VALIDATOR_FACTORY.getValidator());
    }

    private static RestClient.Builder restClientBuilder() {
        SasTokenEndpointJsonMapper jsonMapper =
                new SasTokenEndpointJsonMapper(new ObjectMapper());
        return RestClient.builder()
                .messageConverters(converters -> {
                    converters.removeIf(
                            MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(jsonMapper.messageConverter());
                });
    }

    private static MobileOtpGrantRequest grantRequest() {
        return new MobileOtpGrantRequest(
                1001L,
                "123456",
                "app.api",
                "device-1",
                "PHONE",
                "Test phone",
                "app",
                "IOS",
                "1.0");
    }

    private static MockClientHttpResponse jsonResponse() {
        String json = """
                {
                  "token_type": "Bearer",
                  "access_token": "access-token",
                  "expires_in": 900,
                  "scope": "app.api",
                  "user_id": 100001,
                  "auth_account_id": 200001,
                  "session_id": "session-1",
                  "from_registration_flow": true,
                  "replayed": false
                }
                """;
        MockClientHttpResponse response =
                new MockClientHttpResponse(
                        json.getBytes(StandardCharsets.UTF_8),
                        HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response;
    }

    private static MockClientHttpResponse refreshJsonResponse() {
        String json = """
                {
                  "token_type": "Bearer",
                  "access_token": "access-token-2",
                  "refresh_token": "refresh-token-2",
                  "expires_in": 900,
                  "scope": "app.api"
                }
                """;
        MockClientHttpResponse response = new MockClientHttpResponse(
                json.getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response;
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "test response was not released");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
