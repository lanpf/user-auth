package com.cloud.userauth.infrastructure.external.wechat.miniprogram.verification;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ExternalIdentityVerifier;
import com.cloud.userauth.application.login.external.ExternalIdentityIssuerCodes;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.WechatMiniProgramClient;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuer;
import com.cloud.userauth.domain.authentication.credential.CredentialIssuerType;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.credential.Principal;
import com.cloud.userauth.domain.authentication.external.ExternalIdentity;
import com.cloud.userauth.domain.authentication.external.ProofType;
import jakarta.validation.Validator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class WechatMiniProgramExternalIdentityVerifier
        implements ExternalIdentityVerifier {
    public static final String ISSUER_CODE = ExternalIdentityIssuerCodes.WECHAT_MINI_PROGRAM;
    private static final CredentialIssuer ISSUER =
            new CredentialIssuer(
                    ISSUER_CODE,
                    CredentialIssuerType.PUBLIC_THIRD_PARTY);

    private final WechatMiniProgramClient client;
    private final Validator validator;

    @Override
    public String issuerCode() {
        return ISSUER_CODE;
    }

    @Override
    public ExternalIdentity verify(
            ProofType proofType,
            Map<String, String> proofParameters
    ) {
        if (proofType != ProofType.AUTHORIZATION_CODE) {
            throw rejected();
        }
        WechatMiniProgramProof proof =
                WechatMiniProgramProofMapper.toProof(proofParameters);
        if (proof == null || !CollectionUtils.isEmpty(validator.validate(proof))) {
            throw rejected();
        }
        String openId = client.exchangeLoginCode(proof.loginCode());
        LoginMobile mobile = null;
        boolean mobileVerified = false;
        if (StringUtils.hasText(proof.phoneCode())) {
            mobile = new LoginMobile(
                    client.exchangePhoneCode(proof.phoneCode()));
            mobileVerified = true;
        }
        return new ExternalIdentity(
                ISSUER,
                new Principal(openId),
                mobile,
                mobileVerified);
    }

    private static ApplicationException rejected() {
        return new ApplicationException(ApplicationError.APP_LOGIN_REJECTED);
    }
}
