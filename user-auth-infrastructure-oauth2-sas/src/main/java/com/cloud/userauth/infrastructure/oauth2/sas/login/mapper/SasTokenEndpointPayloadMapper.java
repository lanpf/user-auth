package com.cloud.userauth.infrastructure.oauth2.sas.login.mapper;

import com.cloud.userauth.application.login.MobileOtpLoginOutput;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;

/** SAS Token Endpoint payload 到应用层登录输出的对象映射契约。 */
public interface SasTokenEndpointPayloadMapper {

    MobileOtpLoginOutput toOutput(SasTokenEndpointPayload response);
}
