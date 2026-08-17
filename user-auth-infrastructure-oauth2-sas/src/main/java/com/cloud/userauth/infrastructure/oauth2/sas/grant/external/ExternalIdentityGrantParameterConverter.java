package com.cloud.userauth.infrastructure.oauth2.sas.grant.external;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public final class ExternalIdentityGrantParameterConverter {
    private ExternalIdentityGrantParameterConverter() {
    }

    public static MultiValueMap<String, String> toTokenRequestForm(ExternalIdentityGrantRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(OAuth2ParameterNames.GRANT_TYPE, ExternalIdentityGrantTypes.VALUE);
        form.add(ExternalIdentityGrantParameterNames.LOGIN_ATTEMPT_ID, request.loginAttemptId());
        add(form, ExternalIdentityGrantParameterNames.CHALLENGE_ID, request.challengeId());
        add(form, ExternalIdentityGrantParameterNames.CODE, request.code());
        add(form, OAuth2ParameterNames.SCOPE, request.scope());
        add(form, ExternalIdentityGrantParameterNames.DEVICE_ID, request.deviceId());
        add(form, ExternalIdentityGrantParameterNames.DEVICE_TYPE, request.deviceType());
        add(form, ExternalIdentityGrantParameterNames.DEVICE_NAME, request.deviceName());
        form.add(ExternalIdentityGrantParameterNames.CLIENT_APP_ID, request.clientAppId());
        add(form, ExternalIdentityGrantParameterNames.CLIENT_PLATFORM, request.clientPlatform());
        add(form, ExternalIdentityGrantParameterNames.CLIENT_VERSION, request.clientVersion());
        add(form, ExternalIdentityGrantParameterNames.CHANNEL_CODE, request.channelCode());
        form.add(ExternalIdentityGrantParameterNames.BIND_EXTERNAL_IDENTITY,
                Boolean.toString(request.bindExternalIdentity()));
        return form;
    }

    public static Map<String, Object> toAdditionalParameters(ExternalIdentityGrantRequest request) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(ExternalIdentityGrantParameterNames.LOGIN_ATTEMPT_ID, request.loginAttemptId());
        put(parameters, ExternalIdentityGrantParameterNames.CHALLENGE_ID, request.challengeId());
        put(parameters, ExternalIdentityGrantParameterNames.CODE, request.code());
        put(parameters, OAuth2ParameterNames.SCOPE, request.scope());
        put(parameters, ExternalIdentityGrantParameterNames.DEVICE_ID, request.deviceId());
        put(parameters, ExternalIdentityGrantParameterNames.DEVICE_TYPE, request.deviceType());
        put(parameters, ExternalIdentityGrantParameterNames.DEVICE_NAME, request.deviceName());
        parameters.put(ExternalIdentityGrantParameterNames.CLIENT_APP_ID, request.clientAppId());
        put(parameters, ExternalIdentityGrantParameterNames.CLIENT_PLATFORM, request.clientPlatform());
        put(parameters, ExternalIdentityGrantParameterNames.CLIENT_VERSION, request.clientVersion());
        put(parameters, ExternalIdentityGrantParameterNames.CHANNEL_CODE, request.channelCode());
        parameters.put(ExternalIdentityGrantParameterNames.BIND_EXTERNAL_IDENTITY,
                request.bindExternalIdentity());
        return parameters;
    }

    private static void add(MultiValueMap<String, String> form, String name, Object value) {
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            form.add(name, String.valueOf(value));
        }
    }

    private static void put(Map<String, Object> parameters, String name, Object value) {
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            parameters.put(name, value);
        }
    }
}
