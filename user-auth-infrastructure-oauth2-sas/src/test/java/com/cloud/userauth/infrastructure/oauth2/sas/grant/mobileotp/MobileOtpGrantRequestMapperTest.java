package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.cloud.userauth.application.login.MobileAuthenticationCommand;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantParameterConverter;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantParameterNames;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantTypes;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper.MobileOtpGrantRequestMapper;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.mapper.mapstruct.MobileOtpGrantRequestMapStructMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.MultiValueMap;

class MobileOtpGrantRequestMapperTest {
    private final MobileOtpGrantRequestMapper mapper =
            Mappers.getMapper(MobileOtpGrantRequestMapStructMapper.class);

    @Test
    void shouldMapLoginCommandToGrantRequestAndAuthenticationCommand() {
        MobileOtpGrantRequest request = mapper.toGrantRequest(
                loginCommand(),
                "app.api");

        MobileAuthenticationCommand authenticationCommand =
                mapper.toAuthenticationCommand(request);

        assertEquals(1001L, request.challengeId());
        assertEquals("app.api", request.scope());
        assertEquals("app", request.clientAppId());
        assertEquals("DIRECT", request.channelCode());
        assertEquals(1001L, authenticationCommand.challengeId());
        assertEquals("123456", authenticationCommand.code());
        assertEquals("device-1", authenticationCommand.deviceId());
        assertEquals("app", authenticationCommand.clientAppId());
        assertEquals("DIRECT", authenticationCommand.channelCode());
    }

    @Test
    void shouldEncodeOnlyDefinedProtocolParameters() {
        MobileOtpGrantRequest request = mapper.toGrantRequest(
                loginCommand(),
                "app.api");

        MultiValueMap<String, String> form =
                MobileOtpGrantParameterConverter.toTokenRequestForm(request);
        var additionalParameters =
                MobileOtpGrantParameterConverter.toAdditionalParameters(request);

        assertEquals(
                MobileOtpGrantTypes.VALUE,
                form.getFirst(OAuth2ParameterNames.GRANT_TYPE));
        assertEquals(
                "1001",
                form.getFirst(MobileOtpGrantParameterNames.CHALLENGE_ID));
        assertEquals("app.api", form.getFirst(OAuth2ParameterNames.SCOPE));
        assertEquals(
                "DIRECT",
                form.getFirst(MobileOtpGrantParameterNames.CHANNEL_CODE));
        assertEquals(
                "app",
                additionalParameters.get(
                        MobileOtpGrantParameterNames.CLIENT_APP_ID));
        assertFalse(form.containsKey(MobileOtpGrantParameterNames.DEVICE_TYPE));
        assertFalse(
                additionalParameters.containsKey(
                        MobileOtpGrantParameterNames.CLIENT_VERSION));
    }

    private static MobileOtpLoginCommand loginCommand() {
        return new MobileOtpLoginCommand(
                1001L,
                "123456",
                "device-1",
                null,
                "Test phone",
                "app",
                "IOS",
                null,
                "DIRECT");
    }
}
