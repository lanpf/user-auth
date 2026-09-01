package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramApiPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramCode2SessionPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramPhoneNumberPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramPhoneNumberRequest;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramStableAccessTokenPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramStableAccessTokenRequest;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
public final class WechatMiniProgramApiClientAdapter implements WechatMiniProgramApiClient {
    private static final String STABLE_TOKEN_PATH = "/cgi-bin/stable_token";
    private static final String CODE_TO_SESSION_PATH = "/sns/jscode2session";
    private static final String PHONE_NUMBER_PATH = "/wxa/business/getuserphonenumber";

    private final RestClient restClient;
    private final WechatMiniProgramProperties properties;

    @Override
    public WechatMiniProgramStableAccessTokenPayload getStableAccessToken() {
        WechatMiniProgramStableAccessTokenRequest request =
                new WechatMiniProgramStableAccessTokenRequest(
                        "client_credential",
                        properties.getAppId(),
                        properties.getAppSecret(),
                        false);
        return execute(() -> restClient.post()
                .uri(STABLE_TOKEN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(WechatMiniProgramStableAccessTokenPayload.class));
    }

    @Override
    public WechatMiniProgramCode2SessionPayload exchangeLoginCode(String loginCode) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(CODE_TO_SESSION_PATH)
                        .queryParam("appid", properties.getAppId())
                        .queryParam("secret", properties.getAppSecret())
                        .queryParam("js_code", loginCode)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .retrieve()
                .body(WechatMiniProgramCode2SessionPayload.class));
    }

    @Override
    public WechatMiniProgramPhoneNumberPayload exchangePhoneCode(String accessToken, String phoneCode) {
        return execute(() -> restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(PHONE_NUMBER_PATH)
                        .queryParam("access_token", accessToken)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new WechatMiniProgramPhoneNumberRequest(phoneCode))
                .retrieve()
                .body(WechatMiniProgramPhoneNumberPayload.class));
    }

    private static <T extends WechatMiniProgramApiPayload> T execute(Supplier<T> supplier) {
        try {
            T response = supplier.get();
            WechatMiniProgramApiPayloadValidator.validate(response);
            return response;
        } catch (RestClientException | HttpMessageConversionException exception) {
            throw new InfrastructureException(
                    InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE,
                    exception);
        }
    }

}
