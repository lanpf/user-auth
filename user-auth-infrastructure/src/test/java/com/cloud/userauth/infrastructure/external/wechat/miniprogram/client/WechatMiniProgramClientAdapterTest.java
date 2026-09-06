package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramCode2SessionPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramPhoneNumberPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramStableAccessTokenPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WechatMiniProgramClientAdapterTest {
    private static final int SYSTEM_BUSY = -1;
    private static final int AUTHORIZATION_CODE_INVALID = 40029;
    private static final int ACCESS_TOKEN_INVALID = 40001;

    @Test
    void shouldRejectInvalidLoginAuthorizationCode() {
        StubApiClient apiClient = new StubApiClient();
        apiClient.loginFailure = new WechatMiniProgramApiException(
                AUTHORIZATION_CODE_INVALID,
                "invalid code");

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> client(apiClient).exchangeLoginCode("login-code"));

        assertEquals(
                ApplicationError.APP_LOGIN_REJECTED.errorCode(),
                exception.getErrorCode());
    }

    @Test
    void shouldTranslateUnexpectedLoginProviderErrorToInfrastructureFailure() {
        StubApiClient apiClient = new StubApiClient();
        apiClient.loginFailure = new WechatMiniProgramApiException(
                SYSTEM_BUSY,
                "system busy");

        InfrastructureException exception = assertThrows(
                InfrastructureException.class,
                () -> client(apiClient).exchangeLoginCode("login-code"));

        assertProviderUnavailable(exception);
    }

    @Test
    void shouldTranslateStableAccessTokenFailureToInfrastructureFailure() {
        StubApiClient apiClient = new StubApiClient();
        apiClient.accessTokenFailure = new WechatMiniProgramApiException(
                40013,
                "invalid appid");

        InfrastructureException exception = assertThrows(
                InfrastructureException.class,
                () -> client(apiClient).exchangePhoneCode("phone-code"));

        assertProviderUnavailable(exception);
        assertEquals(0, apiClient.phoneCodeRequests);
    }

    @Test
    void shouldRefreshAccessTokenAndRetryPhoneCodeOnce() {
        StubApiClient apiClient = new StubApiClient();
        apiClient.phoneCodeFailures.add(new WechatMiniProgramApiException(
                ACCESS_TOKEN_INVALID,
                "invalid access token"));

        String mobile = client(apiClient).exchangePhoneCode("phone-code");

        assertEquals("13800138000", mobile);
        assertEquals(2, apiClient.accessTokenRequests);
        assertEquals(2, apiClient.phoneCodeRequests);
        assertEquals(List.of("access-token-1", "access-token-2"), apiClient.usedAccessTokens);
    }

    @Test
    void shouldRejectInvalidPhoneAuthorizationCodeWithoutRetry() {
        StubApiClient apiClient = new StubApiClient();
        apiClient.phoneCodeFailures.add(new WechatMiniProgramApiException(
                AUTHORIZATION_CODE_INVALID,
                "invalid code"));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> client(apiClient).exchangePhoneCode("phone-code"));

        assertEquals(
                ApplicationError.APP_LOGIN_REJECTED.errorCode(),
                exception.getErrorCode());
        assertEquals(1, apiClient.accessTokenRequests);
        assertEquals(1, apiClient.phoneCodeRequests);
    }

    @Test
    void shouldStopAfterOneAccessTokenRefreshRetry() {
        StubApiClient apiClient = new StubApiClient();
        apiClient.phoneCodeFailures.add(new WechatMiniProgramApiException(
                ACCESS_TOKEN_INVALID,
                "invalid access token"));
        apiClient.phoneCodeFailures.add(new WechatMiniProgramApiException(
                ACCESS_TOKEN_INVALID,
                "invalid access token"));

        InfrastructureException exception = assertThrows(
                InfrastructureException.class,
                () -> client(apiClient).exchangePhoneCode("phone-code"));

        assertProviderUnavailable(exception);
        assertEquals(2, apiClient.accessTokenRequests);
        assertEquals(2, apiClient.phoneCodeRequests);
    }

    private static WechatMiniProgramClient client(StubApiClient apiClient) {
        WechatMiniProgramProperties properties = new WechatMiniProgramProperties();
        WechatMiniProgramAccessTokenProvider accessTokenProvider =
                new WechatMiniProgramAccessTokenProvider(
                        apiClient,
                        properties,
                        Clock.fixed(
                                Instant.parse("2026-09-01T08:00:00Z"),
                                ZoneOffset.UTC));
        return new WechatMiniProgramClientAdapter(
                apiClient,
                accessTokenProvider);
    }

    private static void assertProviderUnavailable(InfrastructureException exception) {
        assertEquals(
                InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE.errorCode(),
                exception.getErrorCode());
    }

    private static final class StubApiClient implements WechatMiniProgramApiClient {
        private final List<WechatMiniProgramApiException> phoneCodeFailures =
                new ArrayList<>();
        private final List<String> usedAccessTokens = new ArrayList<>();
        private WechatMiniProgramApiException loginFailure;
        private WechatMiniProgramApiException accessTokenFailure;
        private int accessTokenRequests;
        private int phoneCodeRequests;

        @Override
        public WechatMiniProgramStableAccessTokenPayload getStableAccessToken() {
            accessTokenRequests++;
            if (accessTokenFailure != null) {
                throw accessTokenFailure;
            }
            return new WechatMiniProgramStableAccessTokenPayload(
                    "access-token-" + accessTokenRequests,
                    7200L,
                    null,
                    null);
        }

        @Override
        public WechatMiniProgramCode2SessionPayload exchangeLoginCode(String loginCode) {
            if (loginFailure != null) {
                throw loginFailure;
            }
            return new WechatMiniProgramCode2SessionPayload(
                    "openid-1",
                    "session-key",
                    null,
                    null,
                    null);
        }

        @Override
        public WechatMiniProgramPhoneNumberPayload exchangePhoneCode(
                String accessToken,
                String phoneCode
        ) {
            usedAccessTokens.add(accessToken);
            int failureIndex = phoneCodeRequests;
            phoneCodeRequests++;
            if (failureIndex < phoneCodeFailures.size()) {
                throw phoneCodeFailures.get(failureIndex);
            }
            return new WechatMiniProgramPhoneNumberPayload(
                    new WechatMiniProgramPhoneNumberPayload.WechatPhoneInfo(
                            "13800138000",
                            "13800138000",
                            "86"),
                    0,
                    "ok");
        }
    }
}
