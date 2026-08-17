package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public final class WechatMiniProgramJsonMapper {
    private final ObjectMapper objectMapper;

    WechatMiniProgramJsonMapper(ObjectMapper source) {
        this.objectMapper = source.copy()
                .setPropertyNamingStrategy(
                        new WechatMiniProgramNamingStrategy());
    }

    MappingJackson2HttpMessageConverter messageConverter() {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    private static final class WechatMiniProgramNamingStrategy
            extends PropertyNamingStrategies.NamingBase {
        @Override
        public String translate(String propertyName) {
            return switch (propertyName) {
                case "openId" -> "openid";
                case "sessionKey" -> "session_key";
                case "unionId" -> "unionid";
                case "grantType" -> "grant_type";
                case "appId" -> "appid";
                case "forceRefresh" -> "force_refresh";
                case "accessToken" -> "access_token";
                case "expiresIn" -> "expires_in";
                case "phoneInfo" -> "phone_info";
                case "errorCode" -> "errcode";
                case "errorMessage" -> "errmsg";
                default -> propertyName;
            };
        }
    }
}
