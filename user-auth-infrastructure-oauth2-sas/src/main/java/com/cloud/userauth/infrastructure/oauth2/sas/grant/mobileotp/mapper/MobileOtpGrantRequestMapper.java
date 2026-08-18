package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper;

import com.cloud.userauth.application.login.MobileOtpAuthenticationCommand;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;

/** 应用登录命令与类型化 mobile_otp Grant 请求之间的对象映射契约。 */
public interface MobileOtpGrantRequestMapper {

    MobileOtpGrantRequest toGrantRequest(
            MobileOtpLoginCommand command,
            String scope
    );

    MobileOtpAuthenticationCommand toAuthenticationCommand(
            MobileOtpGrantRequest request
    );
}
