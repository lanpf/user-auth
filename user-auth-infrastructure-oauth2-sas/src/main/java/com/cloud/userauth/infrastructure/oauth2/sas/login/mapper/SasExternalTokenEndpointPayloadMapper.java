package com.cloud.userauth.infrastructure.oauth2.sas.login.mapper;

import com.cloud.userauth.application.login.external.ExternalLoginOutput;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;

public interface SasExternalTokenEndpointPayloadMapper {
    ExternalLoginOutput toOutput(SasTokenEndpointPayload response);
}
