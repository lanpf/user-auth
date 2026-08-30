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
    private final RestClient restClient;
    private final WechatMiniProgramProperties properties;

    @Override
    public WechatStableAccessTokenPayload getStableAccessToken() {
        WechatStableAccessTokenRequest request =
                new WechatStableAccessTokenRequest(
                        WechatMiniProgramApiConstants.CLIENT_CREDENTIAL_GRANT,
                        properties.getAppId(),
                        properties.getAppSecret(),
                        false);
        return execute(() -> restClient.post()
                .uri(WechatMiniProgramApiConstants.STABLE_TOKEN_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(WechatStableAccessTokenPayload.class));
    }

    @Override
    public WechatCode2SessionPayload exchangeLoginCode(String loginCode) {
        return execute(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(WechatMiniProgramApiConstants.CODE_TO_SESSION_PATH)
                        .queryParam(WechatMiniProgramApiConstants.APP_ID_PARAMETER, properties.getAppId())
                        .queryParam(WechatMiniProgramApiConstants.SECRET_PARAMETER, properties.getAppSecret())
                        .queryParam(WechatMiniProgramApiConstants.JS_CODE_PARAMETER, loginCode)
                        .queryParam(WechatMiniProgramApiConstants.GRANT_TYPE_PARAMETER,
                                WechatMiniProgramApiConstants.AUTHORIZATION_CODE_GRANT)
                        .build())
                .retrieve()
                .body(WechatCode2SessionPayload.class));
    }

    @Override
    public WechatPhoneNumberPayload exchangePhoneCode(String accessToken, String phoneCode) {
        return execute(() -> restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(WechatMiniProgramApiConstants.PHONE_NUMBER_PATH)
                        .queryParam(WechatMiniProgramApiConstants.ACCESS_TOKEN_PARAMETER, accessToken)
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
