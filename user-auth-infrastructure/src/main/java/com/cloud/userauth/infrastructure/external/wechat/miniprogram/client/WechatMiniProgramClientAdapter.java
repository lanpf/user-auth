package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramCode2SessionPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramPhoneNumberPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class WechatMiniProgramClientAdapter
        implements WechatMiniProgramClient {

    private final WechatMiniProgramApiClient apiClient;
    private final WechatMiniProgramAccessTokenProvider accessTokenProvider;

    @Override
    public String exchangeLoginCode(String loginCode) {
        try {
            WechatMiniProgramCode2SessionPayload response =
                    apiClient.exchangeLoginCode(loginCode);
            if (!StringUtils.hasText(response.openId())) {
                throw rejected();
            }
            return response.openId();
        } catch (WechatMiniProgramApiException exception) {
            if (exception.isAuthorizationCodeInvalid()) {
                throw rejected(exception);
            }
            throw providerUnavailable(exception);
        }
    }

    @Override
    public String exchangePhoneCode(String phoneCode) {
        String accessToken = accessTokenProvider.getAccessToken();
        try {
            return exchangePhoneCode(accessToken, phoneCode);
        } catch (WechatMiniProgramApiException exception) {
            if (!exception.isAccessTokenInvalid()) {
                throw translatePhoneCodeFailure(exception);
            }
            accessTokenProvider.invalidate(accessToken);
            try {
                return exchangePhoneCode(
                        accessTokenProvider.getAccessToken(),
                        phoneCode);
            } catch (WechatMiniProgramApiException retryException) {
                throw translatePhoneCodeFailure(retryException);
            }
        }
    }

    private String exchangePhoneCode(String accessToken, String phoneCode) {
        WechatMiniProgramPhoneNumberPayload response =
                apiClient.exchangePhoneCode(accessToken, phoneCode);
        if (response.phoneInfo() == null
                || !StringUtils.hasText(response.phoneInfo().phoneNumber())) {
            throw rejected();
        }
        return response.phoneInfo().phoneNumber();
    }

    private static RuntimeException translatePhoneCodeFailure(
            WechatMiniProgramApiException exception
    ) {
        if (exception.isAuthorizationCodeInvalid()) {
            return rejected(exception);
        }
        return providerUnavailable(exception);
    }

    private static ApplicationException rejected() {
        return new ApplicationException(ApplicationError.APP_LOGIN_REJECTED);
    }

    private static ApplicationException rejected(Throwable cause) {
        return new ApplicationException(
                ApplicationError.APP_LOGIN_REJECTED,
                cause);
    }

    private static InfrastructureException providerUnavailable(Throwable cause) {
        return new InfrastructureException(
                InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE,
                cause);
    }
}
