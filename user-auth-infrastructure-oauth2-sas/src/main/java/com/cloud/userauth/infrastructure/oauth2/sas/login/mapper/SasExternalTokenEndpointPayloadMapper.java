package com.cloud.userauth.infrastructure.oauth2.sas.login.mapper;

import com.cloud.userauth.application.login.external.ExternalLoginCommandOutput;
import com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint.SasTokenEndpointPayload;

public interface SasExternalTokenEndpointPayloadMapper {
    ExternalLoginCommandOutput toOutput(SasTokenEndpointPayload response);
}
