package com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantAuthenticationConverter;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantAuthenticationToken;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantParameterNames;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequestParser;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantTypes;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

class MobileOtpGrantAuthenticationConverterTest {
    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();

    private final MobileOtpGrantAuthenticationConverter converter =
            new MobileOtpGrantAuthenticationConverter(
                    new MobileOtpGrantRequestParser(
                            VALIDATOR_FACTORY.getValidator()));

    @BeforeEach
    void setClientPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        "client",
                        null,
                        List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void shouldParseTypedGrantRequest() {
        MockHttpServletRequest request = validRequest();
        request.addParameter(OAuth2ParameterNames.SCOPE, "app.api");

        MobileOtpGrantAuthenticationToken authentication = assertInstanceOf(
                MobileOtpGrantAuthenticationToken.class,
                converter.convert(request));

        assertEquals(1001L, authentication.request().challengeId());
        assertEquals("123456", authentication.request().code());
        assertEquals("app", authentication.request().clientAppId());
        assertEquals("DIRECT", authentication.request().channelCode());
        assertEquals("app.api", authentication.request().scope());
        assertEquals(
                "1001",
                authentication.getAdditionalParameters().get(
                        MobileOtpGrantParameterNames.CHALLENGE_ID));
    }

    @Test
    void shouldRejectNonNumericChallengeIdAsInvalidRequest() {
        MockHttpServletRequest request = validRequest();
        request.setParameter(
                MobileOtpGrantParameterNames.CHALLENGE_ID,
                "not-a-number");

        assertInvalidRequest(() -> converter.convert(request));
    }

    @Test
    void shouldRejectMissingRequiredParameterUsingBeanValidation() {
        MockHttpServletRequest request = validRequest();
        request.removeParameter(MobileOtpGrantParameterNames.CLIENT_APP_ID);

        assertInvalidRequest(() -> converter.convert(request));
    }

    @Test
    void shouldRejectRepeatedParameter() {
        MockHttpServletRequest request = validRequest();
        request.setParameter(
                MobileOtpGrantParameterNames.CODE,
                "123456",
                "654321");

        assertInvalidRequest(() -> converter.convert(request));
    }

    private static MockHttpServletRequest validRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.addParameter(
                OAuth2ParameterNames.GRANT_TYPE,
                MobileOtpGrantTypes.VALUE);
        request.addParameter(
                MobileOtpGrantParameterNames.CHALLENGE_ID,
                "1001");
        request.addParameter(MobileOtpGrantParameterNames.CODE, "123456");
        request.addParameter(MobileOtpGrantParameterNames.CLIENT_APP_ID, "app");
        request.addParameter(MobileOtpGrantParameterNames.CHANNEL_CODE, "DIRECT");
        return request;
    }

    private static void assertInvalidRequest(
            org.junit.jupiter.api.function.Executable executable
    ) {
        OAuth2AuthenticationException exception =
                assertThrows(OAuth2AuthenticationException.class, executable);
        assertEquals(
                OAuth2ErrorCodes.INVALID_REQUEST,
                exception.getError().getErrorCode());
    }
}
