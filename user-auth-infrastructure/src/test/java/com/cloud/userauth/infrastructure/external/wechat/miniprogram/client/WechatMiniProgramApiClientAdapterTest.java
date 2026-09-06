package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramCode2SessionPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramPhoneNumberPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramStableAccessTokenPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class WechatMiniProgramApiClientAdapterTest {
    @Test
    void shouldExchangeLoginCodeUsingWechatProtocol() {
        TestClient fixture = fixture();
        fixture.server.expect(requestTo(
                        "https://api.weixin.qq.com/sns/jscode2session"
                                + "?appid=app-id"
                                + "&secret=app-secret"
                                + "&js_code=login-code"
                                + "&grant_type=authorization_code"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "openid": "openid-1",
                          "session_key": "session-key",
                          "unionid": "unionid-1"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        WechatMiniProgramCode2SessionPayload response =
                fixture.client.exchangeLoginCode("login-code");

        assertEquals("openid-1", response.openId());
        assertEquals("session-key", response.sessionKey());
        assertEquals("unionid-1", response.unionId());
        fixture.server.verify();
    }

    @Test
    void shouldExchangeLoginCodeWhenWechatReturnsJsonAsTextPlain() {
        TestClient fixture = fixture();
        fixture.server.expect(requestTo(
                        "https://api.weixin.qq.com/sns/jscode2session"
                                + "?appid=app-id"
                                + "&secret=app-secret"
                                + "&js_code=login-code"
                                + "&grant_type=authorization_code"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "openid": "openid-1",
                          "session_key": "session-key",
                          "unionid": "unionid-1"
                        }
                        """,
                        MediaType.TEXT_PLAIN));

        WechatMiniProgramCode2SessionPayload response =
                fixture.client.exchangeLoginCode("login-code");

        assertEquals("openid-1", response.openId());
        assertEquals("session-key", response.sessionKey());
        assertEquals("unionid-1", response.unionId());
        fixture.server.verify();
    }

    @Test
    void shouldTranslateMalformedWechatPayloadToInfrastructureFailure() {
        TestClient fixture = fixture();
        fixture.server.expect(requestTo(
                        "https://api.weixin.qq.com/sns/jscode2session"
                                + "?appid=app-id"
                                + "&secret=app-secret"
                                + "&js_code=login-code"
                                + "&grant_type=authorization_code"))
                .andRespond(withSuccess(
                        "not-json",
                        MediaType.TEXT_PLAIN));

        InfrastructureException exception = assertThrows(
                InfrastructureException.class,
                () -> fixture.client.exchangeLoginCode("login-code"));

        assertEquals(
                InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE.errorCode(),
                exception.getErrorCode());
        fixture.server.verify();
    }

    @Test
    void shouldRejectWechatErrorWhenExchangingLoginCode() {
        TestClient fixture = fixture();
        fixture.server.expect(requestTo(
                        "https://api.weixin.qq.com/sns/jscode2session"
                                + "?appid=app-id"
                                + "&secret=app-secret"
                                + "&js_code=invalid-login-code"
                                + "&grant_type=authorization_code"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "errcode": 40029,
                          "errmsg": "invalid code"
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        WechatMiniProgramApiException exception = assertThrows(
                WechatMiniProgramApiException.class,
                () -> fixture.client.exchangeLoginCode("invalid-login-code"));

        assertEquals(40029, exception.getErrorCode());
        assertEquals("invalid code", exception.getMessage());
        fixture.server.verify();
    }

    @Test
    void shouldRequestStableTokenAndExchangePhoneCode() {
        TestClient fixture = fixture();
        fixture.server.expect(requestTo(
                        "https://api.weixin.qq.com/cgi-bin/stable_token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(
                        "{\"grant_type\":\"client_credential\","
                                + "\"appid\":\"app-id\","
                                + "\"secret\":\"app-secret\","
                                + "\"force_refresh\":false}"))
                .andRespond(withSuccess(
                        """
                        {
                          "access_token": "access-token",
                          "expires_in": 7200
                        }
                        """,
                        MediaType.APPLICATION_JSON));
        fixture.server.expect(requestTo(
                        "https://api.weixin.qq.com/wxa/business/getuserphonenumber"
                                + "?access_token=access-token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(
                        "{\"code\":\"phone-code\"}"))
                .andRespond(withSuccess(
                        """
                        {
                          "errcode": 0,
                          "errmsg": "ok",
                          "phone_info": {
                            "phoneNumber": "13800138000",
                            "purePhoneNumber": "13800138000",
                            "countryCode": "86"
                          }
                        }
                        """,
                        MediaType.APPLICATION_JSON));

        WechatMiniProgramStableAccessTokenPayload token =
                fixture.client.getStableAccessToken();
        WechatMiniProgramPhoneNumberPayload phone =
                fixture.client.exchangePhoneCode(
                        token.accessToken(),
                        "phone-code");

        assertEquals(7200L, token.expiresIn());
        assertEquals(
                "13800138000",
                phone.phoneInfo().phoneNumber());
        assertEquals(0, phone.errorCode());
        assertEquals("ok", phone.errorMessage());
        fixture.server.verify();
    }

    private static TestClient fixture() {
        WechatMiniProgramProperties properties =
                new WechatMiniProgramProperties();
        properties.setAppId("app-id");
        properties.setAppSecret("app-secret");
        WechatMiniProgramJsonMapper jsonMapper =
                new WechatMiniProgramJsonMapper(new ObjectMapper());
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.weixin.qq.com")
                .messageConverters(converters -> {
                    converters.removeIf(
                            MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(jsonMapper.messageConverter());
                });
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();
        WechatMiniProgramApiClientAdapter client =
                new WechatMiniProgramApiClientAdapter(
                        builder.build(),
                        properties);
        return new TestClient(client, server);
    }

    private record TestClient(
            WechatMiniProgramApiClientAdapter client,
            MockRestServiceServer server
    ) {
    }
}
