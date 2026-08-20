package com.cloud.userauth.infrastructure.external.wechat.miniprogram.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuerType;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.ProofType;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.WechatMiniProgramClient;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Map;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;

class WechatMiniProgramExternalIdentityVerifierTest {
    private static final Validator VALIDATOR =
            Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(
                            new ParameterMessageInterpolator())
                    .buildValidatorFactory()
                    .getValidator();

    @Test
    void shouldMapWechatIdentityWithVerifiedMobile() {
        StubWechatMiniProgramClient client =
                new StubWechatMiniProgramClient();
        WechatMiniProgramExternalIdentityVerifier verifier =
                verifier(client);

        ExternalIdentity identity = verifier.verify(
                ProofType.AUTHORIZATION_CODE,
                Map.of(
                        WechatMiniProgramProofParameters.LOGIN_CODE,
                        "login-code",
                        WechatMiniProgramProofParameters.PHONE_CODE,
                        "phone-code"));

        assertEquals(
                WechatMiniProgramExternalIdentityVerifier.ISSUER_CODE,
                identity.issuer().code());
        assertEquals(
                CredentialIssuerType.PUBLIC_THIRD_PARTY,
                identity.issuer().issuerType());
        assertEquals("openid-1", identity.principal().value());
        assertEquals("13800138000", identity.mobile().value());
        assertTrue(identity.mobileVerified());
        assertEquals(1, client.phoneExchangeCount);
    }

    @Test
    void shouldAllowLoginProofWithoutPhoneCode() {
        StubWechatMiniProgramClient client =
                new StubWechatMiniProgramClient();

        ExternalIdentity identity = verifier(client).verify(
                ProofType.AUTHORIZATION_CODE,
                Map.of(
                        WechatMiniProgramProofParameters.LOGIN_CODE,
                        "login-code"));

        assertNull(identity.mobile());
        assertFalse(identity.mobileVerified());
        assertEquals(0, client.phoneExchangeCount);
    }

    @Test
    void shouldRejectUnsupportedProofTypeAndMissingLoginCode() {
        WechatMiniProgramExternalIdentityVerifier verifier =
                verifier(new StubWechatMiniProgramClient());

        ApplicationException unsupported = assertThrows(
                ApplicationException.class,
                () -> verifier.verify(
                        ProofType.SIGNED_ASSERTION,
                        Map.of(
                                WechatMiniProgramProofParameters.LOGIN_CODE,
                                "login-code")));
        ApplicationException invalid = assertThrows(
                ApplicationException.class,
                () -> verifier.verify(
                        ProofType.AUTHORIZATION_CODE,
                        Map.of(
                                WechatMiniProgramProofParameters.PHONE_CODE,
                                "phone-code")));

        assertEquals(ApplicationError.APP_LOGIN_REJECTED.errorCode(), unsupported.getErrorCode());
        assertEquals(ApplicationError.APP_LOGIN_REJECTED.errorCode(), invalid.getErrorCode());
    }

    private static WechatMiniProgramExternalIdentityVerifier verifier(
            WechatMiniProgramClient client
    ) {
        return new WechatMiniProgramExternalIdentityVerifier(
                client,
                VALIDATOR);
    }

    private static final class StubWechatMiniProgramClient
            implements WechatMiniProgramClient {
        private int phoneExchangeCount;

        @Override
        public String exchangeLoginCode(String loginCode) {
            assertEquals("login-code", loginCode);
            return "openid-1";
        }

        @Override
        public String exchangePhoneCode(String phoneCode) {
            assertEquals("phone-code", phoneCode);
            phoneExchangeCount++;
            return "13800138000";
        }
    }
}
