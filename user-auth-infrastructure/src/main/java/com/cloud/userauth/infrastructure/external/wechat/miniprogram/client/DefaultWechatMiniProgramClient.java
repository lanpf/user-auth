package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class DefaultWechatMiniProgramClient
        implements WechatMiniProgramClient {
    private static final int WECHAT_SYSTEM_BUSY = -1;

    private final WechatMiniProgramApiClient apiClient;
    private final WechatMiniProgramAccessTokenProvider accessTokenProvider;

    @Override
    public String exchangeLoginCode(String loginCode) {
        try {
            WechatCode2SessionPayload response =
                    apiClient.exchangeLoginCode(loginCode);
            if (!StringUtils.hasText(response.openId())) {
                throw rejected();
            }
            return response.openId();
        } catch (WechatMiniProgramApiException exception) {
            throw translate(exception);
        }
    }

    @Override
    public String exchangePhoneCode(String phoneCode) {
        try {
            WechatPhoneNumberPayload response =
                    apiClient.exchangePhoneCode(
                            accessTokenProvider.getAccessToken(),
                            phoneCode);
            if (response.phoneInfo() == null
                    || !StringUtils.hasText(
                    response.phoneInfo().phoneNumber())) {
                throw rejected();
            }
            return response.phoneInfo().phoneNumber();
        } catch (WechatMiniProgramApiException exception) {
            throw translate(exception);
        }
    }

    private static RuntimeException translate(
            WechatMiniProgramApiException exception
    ) {
        if (exception.getErrorCode() != null
                && exception.getErrorCode() == WECHAT_SYSTEM_BUSY) {
            return new InfrastructureException(
                    InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE,
                    exception);
        }
        return new ApplicationException(
                ApplicationError.APP_LOGIN_REJECTED,
                exception);
    }

    private static ApplicationException rejected() {
        return new ApplicationException(ApplicationError.APP_LOGIN_REJECTED);
    }
}
