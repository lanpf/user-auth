package com.cloud.userauth.infrastructure.oauth2.sas.login.mapper;

import com.cloud.userauth.application.login.MobileOtpLoginCommandOutput;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;

/** SAS Token Endpoint payload 到应用层登录输出的对象映射契约。 */
public interface SasTokenEndpointPayloadMapper {

    MobileOtpLoginCommandOutput toOutput(SasTokenEndpointPayload response);
}
