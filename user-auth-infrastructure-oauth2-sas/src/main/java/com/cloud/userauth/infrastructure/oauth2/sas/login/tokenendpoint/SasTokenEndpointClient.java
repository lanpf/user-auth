package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantRequest;

/** 调用 SAS 标准 Token Endpoint 的客户端契约。 */
public interface SasTokenEndpointClient {

    SasTokenEndpointPayload requestToken(MobileOtpGrantRequest request);

    SasTokenEndpointPayload requestExternalToken(ExternalIdentityGrantRequest request);

    SasRefreshTokenEndpointPayload requestRefreshToken(String refreshToken);
}
