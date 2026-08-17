package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

record SasErrorPayload(
        String error,
        String errorDescription
) {
}
