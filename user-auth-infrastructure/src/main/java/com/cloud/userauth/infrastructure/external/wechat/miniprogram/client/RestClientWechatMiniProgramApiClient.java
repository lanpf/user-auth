package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@RequiredArgsConstructor
public final class RestClientWechatMiniProgramApiClient implements WechatMiniProgramApiClient {
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CLIENT_CREDENTIAL = "client_credential";

    private final RestClient restClient;
    private final WechatMiniProgramProperties properties;

    @Override
    public WechatStableAccessTokenPayload getStableAccessToken() {
        WechatStableAccessTokenRequest request =
                new WechatStableAccessTokenRequest(
                        CLIENT_CREDENTIAL,
                        properties.getAppId(),
                        properties.getAppSecret(),
                        false);
        return execute(() -> restClient.post()
                .uri("/cgi-bin/stable_token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(WechatStableAccessTokenPayload.class));
    }

    @Override
    public WechatCode2SessionPayload exchangeLoginCode(String loginCode) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sns/jscode2session")
                        .queryParam("appid", properties.getAppId())
                        .queryParam("secret", properties.getAppSecret())
                        .queryParam("js_code", loginCode)
                        .queryParam("grant_type", AUTHORIZATION_CODE)
                        .build())
                .retrieve()
                .body(WechatCode2SessionPayload.class));
    }

    @Override
    public WechatPhoneNumberPayload exchangePhoneCode(String accessToken, String phoneCode) {
        return execute(() -> restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/wxa/business/getuserphonenumber")
                        .queryParam("access_token", accessToken)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new WechatPhoneNumberRequest(phoneCode))
                .retrieve()
                .body(WechatPhoneNumberPayload.class));
    }

    private static <T extends WechatMiniProgramApiPayload> T execute(Supplier<T> supplier) {
        try {
            T response = supplier.get();
            WechatMiniProgramApiPayloadValidator.validate(response);
            return response;
        } catch (RestClientException exception) {
            throw new InfrastructureException(
                    InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE,
                    exception);
        }
    }

}
