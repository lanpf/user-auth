package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramCode2SessionPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

class WechatMiniProgramApiClientAdapterLiveIT {
    private static final String APP_ID_ENV = "WECHAT_MINI_PROGRAM_APP_ID";
    private static final String APP_SECRET_ENV = "WECHAT_MINI_PROGRAM_APP_SECRET";
    private static final String LOGIN_CODE_ENV = "WECHAT_MINI_PROGRAM_LOGIN_CODE";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    @Test
    void shouldExchangeRealWechatLoginCode() {
        String appId = System.getenv(APP_ID_ENV);
        String appSecret = System.getenv(APP_SECRET_ENV);
        String loginCode = System.getenv(LOGIN_CODE_ENV);
        assumeTrue(
                Stream.of(appId, appSecret, loginCode)
                        .allMatch(StringUtils::hasText),
                "real WeChat credentials are not configured");

        WechatMiniProgramCode2SessionPayload response = client(appId, appSecret)
                .exchangeLoginCode(loginCode);

        assertAll(
                () -> assertTrue(
                        StringUtils.hasText(response.openId()),
                        "WeChat response must contain openid"),
                () -> assertTrue(
                        StringUtils.hasText(response.sessionKey()),
                        "WeChat response must contain session_key"));
        System.out.printf(
                "real WeChat code exchange completed: openIdPresent=%s, "
                        + "sessionKeyPresent=%s, unionIdPresent=%s%n",
                StringUtils.hasText(response.openId()),
                StringUtils.hasText(response.sessionKey()),
                StringUtils.hasText(response.unionId()));
    }

    private static WechatMiniProgramApiClientAdapter client(
            String appId,
            String appSecret
    ) {
        WechatMiniProgramProperties properties =
                new WechatMiniProgramProperties();
        properties.setAppId(appId);
        properties.setAppSecret(appSecret);

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        WechatMiniProgramJsonMapper jsonMapper =
                new WechatMiniProgramJsonMapper(new ObjectMapper());
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.weixin.qq.com")
                .requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.removeIf(
                            MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(jsonMapper.messageConverter());
                })
                .build();
        return new WechatMiniProgramApiClientAdapter(
                restClient,
                properties);
    }
}
