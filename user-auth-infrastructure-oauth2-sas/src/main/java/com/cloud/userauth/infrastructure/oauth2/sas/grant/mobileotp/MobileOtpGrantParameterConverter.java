package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 在类型化 mobile_otp Grant 请求与 SAS 协议参数表示之间转换。
 *
 * <p>该类只负责编码协议参数，不负责 Servlet 请求解析或应用命令映射。
 */
public final class MobileOtpGrantParameterConverter {
    private MobileOtpGrantParameterConverter() {
    }

    public static MultiValueMap<String, String> toTokenRequestForm(
            MobileOtpGrantRequest request
    ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add(OAuth2ParameterNames.GRANT_TYPE, MobileOtpGrantTypes.VALUE);
        form.add(
                MobileOtpGrantParameterNames.CHALLENGE_ID,
                String.valueOf(request.challengeId()));
        form.add(MobileOtpGrantParameterNames.CODE, request.code());
        addIfHasText(form, OAuth2ParameterNames.SCOPE, request.scope());
        addIfHasText(
                form,
                MobileOtpGrantParameterNames.DEVICE_ID,
                request.deviceId());
        addIfHasText(
                form,
                MobileOtpGrantParameterNames.DEVICE_TYPE,
                request.deviceType());
        addIfHasText(
                form,
                MobileOtpGrantParameterNames.DEVICE_NAME,
                request.deviceName());
        form.add(
                MobileOtpGrantParameterNames.CLIENT_APP_ID,
                request.clientAppId());
        addIfHasText(
                form,
                MobileOtpGrantParameterNames.CLIENT_PLATFORM,
                request.clientPlatform());
        addIfHasText(
                form,
                MobileOtpGrantParameterNames.CLIENT_VERSION,
                request.clientVersion());
        addIfHasText(
                form,
                MobileOtpGrantParameterNames.CHANNEL_CODE,
                request.channelCode());
        return form;
    }

    public static Map<String, Object> toAdditionalParameters(
            MobileOtpGrantRequest request
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(
                MobileOtpGrantParameterNames.CHALLENGE_ID,
                String.valueOf(request.challengeId()));
        parameters.put(MobileOtpGrantParameterNames.CODE, request.code());
        putIfHasText(parameters, OAuth2ParameterNames.SCOPE, request.scope());
        putIfHasText(
                parameters,
                MobileOtpGrantParameterNames.DEVICE_ID,
                request.deviceId());
        putIfHasText(
                parameters,
                MobileOtpGrantParameterNames.DEVICE_TYPE,
                request.deviceType());
        putIfHasText(
                parameters,
                MobileOtpGrantParameterNames.DEVICE_NAME,
                request.deviceName());
        parameters.put(
                MobileOtpGrantParameterNames.CLIENT_APP_ID,
                request.clientAppId());
        putIfHasText(
                parameters,
                MobileOtpGrantParameterNames.CLIENT_PLATFORM,
                request.clientPlatform());
        putIfHasText(
                parameters,
                MobileOtpGrantParameterNames.CLIENT_VERSION,
                request.clientVersion());
        putIfHasText(
                parameters,
                MobileOtpGrantParameterNames.CHANNEL_CODE,
                request.channelCode());
        return parameters;
    }

    private static void addIfHasText(
            MultiValueMap<String, String> form,
            String name,
            String value
    ) {
        if (StringUtils.hasText(value)) {
            form.add(name, value);
        }
    }

    private static void putIfHasText(
            Map<String, Object> parameters,
            String name,
            String value
    ) {
        if (StringUtils.hasText(value)) {
            parameters.put(name, value);
        }
    }
}
