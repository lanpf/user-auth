package com.cloud.userauth.application.login;

import com.cloud.userauth.application.port.LoginTokenIssuer;
import lombok.RequiredArgsConstructor;

/**
 * 公开手机号登录写用例的唯一 application 入口。
 */
@RequiredArgsConstructor
public class MobileOtpLoginCommandService {
    private final LoginTokenIssuer loginTokenIssuer;

    public MobileOtpLoginOutput execute(MobileOtpLoginCommand command) {
        return loginTokenIssuer.issueMobileOtpLogin(command);
    }
}
