package com.cloud.userauth.application.port;

import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginOutput;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginOutput;
import com.cloud.userauth.application.login.external.ExternalCredentialBinding;
import jakarta.validation.Valid;

/**
 * 面向公开手机号登录用例的可替换安全 Provider。
 *
 * <p>SAS 实现通过标准 OAuth2 Token Endpoint 完成登录和 Token 签发；
 * 其他安全实现必须保持相同的业务输入输出语义。</p>
 */
public interface LoginTokenIssuer {
    MobileOtpLoginOutput issueMobileOtpLogin(@Valid MobileOtpLoginCommand command);

    ExternalLoginOutput issueExternalLogin(
            @Valid ExternalLoginCommand command,
            ExternalCredentialBinding credentialBinding
    );
}
