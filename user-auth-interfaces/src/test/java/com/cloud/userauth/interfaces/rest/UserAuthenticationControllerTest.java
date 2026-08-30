package com.cloud.userauth.interfaces.rest;

import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.cloud.framework.core.Result;
import com.cloud.framework.core.RequestHeader;
import com.cloud.framework.starter.webmvc.client.ClientRequestBodyAdvice;
import com.cloud.framework.starter.webmvc.client.ClientRequestArgumentResolver;
import com.cloud.userauth.api.authentication.BoundExternalCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalProofLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedPartnerMobileLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.enums.LoginSessionStatusApiEnum;
import com.cloud.userauth.interfaces.mapper.AuthenticationRestMapper;
import com.cloud.userauth.interfaces.mapper.mapstruct.AuthenticationRestMapStructMapper;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserAuthenticationControllerTest {

    @Test
    void shouldPublishMobileOtpLoginEndpoint() throws Exception {
        AtomicReference<MobileOtpLoginApiCommand> capturedRequest =
                new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(capturedRequest);
        MockMvc mockMvc = mockMvc(facade);

        MvcResult result = mockMvc.perform(post(UserAuthPathApiConstants.API_LOGIN_MOBILE_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "gateway-app")
                        .header(RequestHeader.CLIENT_PLATFORM, "WECHAT_MINI_PROGRAM")
                        .header(RequestHeader.CLIENT_VERSION, "2.0")
                        .header(RequestHeader.CHANNEL_CODE, "DIRECT")
                        .content("""
                                {
                                  "challengeId": 1001,
                                  "code": "123456",
                                  "deviceId": "device-1",
                                  "clientAppId": "user-app",
                                  "clientPlatform": "IOS",
                                  "clientVersion": "1.0"
                                }
                                """))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(1001L, capturedRequest.get().challengeId());
        assertEquals("123456", capturedRequest.get().code());
        assertEquals("device-1", capturedRequest.get().deviceId());
        assertEquals("gateway-app", capturedRequest.get().clientAppId());
        assertEquals("WECHAT_MINI_PROGRAM", capturedRequest.get().clientPlatform());
        assertEquals("2.0", capturedRequest.get().clientVersion());
        assertEquals("DIRECT", capturedRequest.get().channelCode());
        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"accessToken\":\"access-token\""));
        assertTrue(response.contains("\"fromRegistrationFlow\":true"));
    }

    @Test
    void shouldPublishRefreshEndpointWithGatewayClientAppId() throws Exception {
        AtomicReference<RefreshTokenLoginApiCommand> capturedRequest = new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(new AtomicReference<>()) {
            @Override
            public Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(RefreshTokenLoginApiCommand request) {
                capturedRequest.set(request);
                return Result.success(new RefreshTokenLoginApiCommandOutput(
                        "Bearer", "access-2", "refresh-2", 900L, "app",
                        100001L, 1001L, "session-1"));
            }
        };
        MockMvc mockMvc = mockMvc(facade);

        MvcResult result = mockMvc.perform(post(UserAuthPathApiConstants.API_LOGIN_REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "gateway-app")
                        .content("{\"refreshToken\":\"refresh-1\",\"clientAppId\":\"forged-app\"}"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("gateway-app", capturedRequest.get().clientAppId());
        assertEquals("refresh-1", capturedRequest.get().refreshToken());
        assertTrue(result.getResponse().getContentAsString().contains("\"refreshToken\":\"refresh-2\""));
    }

    @Test
    void shouldPublishTrustedPartnerMobileLoginEndpointWithVerifiedContext() throws Exception {
        AtomicReference<TrustedPartnerMobileLoginApiCommand> capturedRequest = new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(new AtomicReference<>()) {
            @Override
            public Result<ExternalLoginApiCommandOutput> loginWithTrustedPartnerMobile(
                    TrustedPartnerMobileLoginApiCommand request
            ) {
                capturedRequest.set(request);
                return Result.success(new ExternalLoginApiCommandOutput(
                        "Bearer", "access-token", null, 900L, "app", 100001L, 1001L, "session-1"));
            }
        };
        MockMvc mockMvc = mockMvc(facade);

        MvcResult result = mockMvc.perform(post(
                        UserAuthPathApiConstants.API_LOGIN_PARTNER_TRUSTED_MOBILE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "partner-service")
                        .header(RequestHeader.CLIENT_PLATFORM, "SERVICE")
                        .header(RequestHeader.CLIENT_VERSION, "1.0")
                        .header(RequestHeader.CHANNEL_CODE, "PARTNER_A")
                        .content("""
                                {
                                  "partnerCode": "PARTNER_A",
                                  "partnerBizId": "order-1",
                                  "mobile": "13800138000",
                                  "deviceId": "partner-device"
                                }
                                """))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("PARTNER_A", capturedRequest.get().partnerCode());
        assertEquals("order-1", capturedRequest.get().partnerBizId());
        assertEquals("13800138000", capturedRequest.get().mobile());
        assertEquals("partner-device", capturedRequest.get().deviceId());
        assertEquals("partner-service", capturedRequest.get().clientAppId());
        assertEquals("PARTNER_A", capturedRequest.get().channelCode());
    }

    @Test
    void shouldBuildBindExternalCredentialCommandFromTrustedGatewayHeaders() throws Exception {
        AtomicReference<BindExternalCredentialApiCommand> capturedRequest = new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(new AtomicReference<>()) {
            @Override
            public Result<Void> bindExternalCredential(BindExternalCredentialApiCommand request) {
                capturedRequest.set(request);
                return Result.success();
            }
        };
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserAuthenticationController(facade, authenticationRestMapper()))
                .setControllerAdvice(new ClientRequestBodyAdvice())
                .setCustomArgumentResolvers(new ClientRequestArgumentResolver())
                .build();

        MvcResult result = mockMvc.perform(post(UserAuthPathApiConstants.API_CREDENTIALS_BIND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "gateway-app")
                        .header(RequestHeader.CHANNEL_CODE, "DIRECT")
                        .header(RequestHeader.USER_ID, "1001")
                        .header(RequestHeader.SESSION_ID, "session-1")
                        .content("""
                                {
                                  "issuer": "WECHAT_MINI_PROGRAM",
                                  "authorizationCode": "code-1",
                                  "userId": "9999",
                                  "sessionId": "forged-session"
                                }
                                """))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(1001L, capturedRequest.get().authenticatedUserId());
        assertEquals("session-1", capturedRequest.get().authenticatedSessionId());
        assertEquals("WECHAT_MINI_PROGRAM", capturedRequest.get().issuer());
        assertEquals("code-1", capturedRequest.get().authorizationCode());
    }

    @Test
    void shouldRejectMobileOtpLoginWithoutChallengeId() throws Exception {
        AtomicReference<MobileOtpLoginApiCommand> capturedRequest =
                new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(capturedRequest);
        MockMvc mockMvc = mockMvc(facade);

        MvcResult result = mockMvc.perform(post(UserAuthPathApiConstants.API_LOGIN_MOBILE_OTP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "123456"
                                }
                                """))
                .andReturn();

        assertEquals(400, result.getResponse().getStatus());
        assertNull(capturedRequest.get());
    }

    @Test
    void shouldPublishStructuredExternalProofParameters() throws Exception {
        AtomicReference<ExternalAttemptLoginApiCommand> capturedRequest =
                new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(
                new AtomicReference<>(), capturedRequest);
        MockMvc mockMvc = mockMvc(facade);

        MvcResult result = mockMvc.perform(post(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_ATTEMPT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "gateway-app")
                        .header(RequestHeader.CHANNEL_CODE, "DIRECT")
                        .content("""
                                {
                                  "issuer": "WECHAT_MINI_PROGRAM_MAIN",
                                  "proofType": "AUTHORIZATION_CODE",
                                  "proofParameters": {
                                    "loginCode": "login-code",
                                    "phoneCode": "phone-code"
                                  }
                                }
                                """))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
                "login-code",
                capturedRequest.get().proofParameters().get("loginCode"));
        assertEquals(
                "phone-code",
                capturedRequest.get().proofParameters().get("phoneCode"));
    }

    @Test
    void shouldPublishSingleStepExternalProofLoginWithClientAndDeviceContext() throws Exception {
        AtomicReference<ExternalProofLoginApiCommand> capturedRequest = new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(
                new AtomicReference<>()) {
            @Override
            public Result<ExternalLoginApiCommandOutput> loginWithExternalProof(
                    ExternalProofLoginApiCommand request
            ) {
                capturedRequest.set(request);
                return Result.success(new ExternalLoginApiCommandOutput(
                        "Bearer", "access-token", null, 900L, "app",
                        100001L, 1001L, "session-1"));
            }
        };
        MockMvc mockMvc = mockMvc(facade);

        MvcResult result = mockMvc.perform(post(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_PROOF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "mini-program")
                        .header(RequestHeader.CLIENT_PLATFORM, "WECHAT_MINI_PROGRAM")
                        .header(RequestHeader.CLIENT_VERSION, "2.1")
                        .header(RequestHeader.CHANNEL_CODE, "DIRECT")
                        .content("""
                                {
                                  "issuer": "WECHAT_MINI_PROGRAM",
                                  "proofType": "AUTHORIZATION_CODE",
                                  "proofParameters": {
                                    "loginCode": "login-code",
                                    "phoneCode": "phone-code"
                                  },
                                  "deviceId": "device-1",
                                  "deviceType": "PHONE",
                                  "deviceName": "mini program device"
                                }
                                """))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("login-code", capturedRequest.get().proofParameters().get("loginCode"));
        assertEquals("phone-code", capturedRequest.get().proofParameters().get("phoneCode"));
        assertEquals("device-1", capturedRequest.get().deviceId());
        assertEquals("mini-program", capturedRequest.get().clientAppId());
        assertEquals("WECHAT_MINI_PROGRAM", capturedRequest.get().clientPlatform());
        assertEquals("DIRECT", capturedRequest.get().channelCode());
        assertTrue(result.getResponse().getContentAsString().contains("\"accessToken\":\"access-token\""));
    }

    @Test
    void shouldPublishLogoutEndpoint() throws Exception {
        AtomicReference<LogoutApiCommand> capturedRequest = new AtomicReference<>();
        UserAuthenticationCommandFacade facade = new StubUserAuthenticationCommandFacade(
                new AtomicReference<>(), new AtomicReference<>(), capturedRequest);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new UserAuthenticationController(facade, authenticationRestMapper()))
                .setControllerAdvice(new ClientRequestBodyAdvice())
                .setCustomArgumentResolvers(new ClientRequestArgumentResolver())
                .build();

        MvcResult result = mockMvc.perform(post(UserAuthPathApiConstants.API_LOGOUT)
                        .header(RequestHeader.CLIENT_APP_ID, "gateway-app")
                        .header(RequestHeader.USER_ID, "1001")
                        .header(RequestHeader.SESSION_ID, "session-1"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(1001L, capturedRequest.get().authenticatedUserId());
        assertEquals("session-1", capturedRequest.get().sessionId());
        assertTrue(result.getResponse().getContentAsString().contains("\"sessionStatus\":\"REVOKED\""));
    }

    private static MockMvc mockMvc(UserAuthenticationCommandFacade facade) {
        return MockMvcBuilders
                .standaloneSetup(new UserAuthenticationController(facade, authenticationRestMapper()))
                .setControllerAdvice(new ClientRequestBodyAdvice())
                .build();
    }

    private static AuthenticationRestMapper authenticationRestMapper() {
        return Mappers.getMapper(AuthenticationRestMapStructMapper.class);
    }

    private static class StubUserAuthenticationCommandFacade
            implements UserAuthenticationCommandFacade {
        private final AtomicReference<MobileOtpLoginApiCommand> capturedRequest;
        private final AtomicReference<ExternalAttemptLoginApiCommand> capturedExternalLoginAttempt;
        private final AtomicReference<LogoutApiCommand> capturedLogout;

        private StubUserAuthenticationCommandFacade(
                AtomicReference<MobileOtpLoginApiCommand> capturedRequest
        ) {
            this(capturedRequest, new AtomicReference<>(), new AtomicReference<>());
        }

        private StubUserAuthenticationCommandFacade(
                AtomicReference<MobileOtpLoginApiCommand> capturedRequest,
                AtomicReference<ExternalAttemptLoginApiCommand> capturedExternalLoginAttempt
        ) {
            this(capturedRequest, capturedExternalLoginAttempt, new AtomicReference<>());
        }

        private StubUserAuthenticationCommandFacade(
                AtomicReference<MobileOtpLoginApiCommand> capturedRequest,
                AtomicReference<ExternalAttemptLoginApiCommand> capturedExternalLoginAttempt,
                AtomicReference<LogoutApiCommand> capturedLogout
        ) {
            this.capturedRequest = capturedRequest;
            this.capturedExternalLoginAttempt = capturedExternalLoginAttempt;
            this.capturedLogout = capturedLogout;
        }

        @Override
        public Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(
                IssueAuthChallengeApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<MobileOtpLoginApiCommandOutput> completeMobileOtpLogin(
                MobileOtpLoginApiCommand request
        ) {
            capturedRequest.set(request);
            return Result.success(new MobileOtpLoginApiCommandOutput(
                    "Bearer",
                    "access-token",
                    null,
                    900L,
                    "app",
                    100001L,
                    1001L,
                    "session-1",
                    true,
                    false));
        }

        @Override
        public Result<ExternalAttemptLoginApiCommandOutput> attemptExternalLogin(
                ExternalAttemptLoginApiCommand request
        ) {
            capturedExternalLoginAttempt.set(request);
            return Result.success(new ExternalAttemptLoginApiCommandOutput(
                    "login-attempt-session-1",
                    "13800138000",
                    false,
                    Instant.parse("2026-07-29T08:00:00Z")));
        }

        @Override
        public Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(RefreshTokenLoginApiCommand request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<ExternalLoginApiCommandOutput> completeExternalLogin(
                ExternalLoginApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<ExternalLoginApiCommandOutput> loginWithTrustedPartnerMobile(
                TrustedPartnerMobileLoginApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<ExternalLoginApiCommandOutput> loginWithExternalProof(
                ExternalProofLoginApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<ExternalLoginApiCommandOutput> loginWithBoundExternalCredential(
                BoundExternalCredentialLoginApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<Void> bindExternalCredential(
                BindExternalCredentialApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<LogoutApiCommandOutput> logout(LogoutApiCommand request) {
            capturedLogout.set(request);
            return Result.success(new LogoutApiCommandOutput(
                    "session-1", LoginSessionStatusApiEnum.REVOKED));
        }
    }
}
