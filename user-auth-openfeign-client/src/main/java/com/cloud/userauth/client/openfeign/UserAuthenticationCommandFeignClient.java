package com.cloud.userauth.client.openfeign;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.api.authentication.BoundExternalCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalProofLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.TrustedPartnerMobileLoginApiCommand;
import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = UserAuthPathApiConstants.SERVICE_NAME, contextId = "userAuthenticationCommandFeignClient")
public interface UserAuthenticationCommandFeignClient extends UserAuthenticationCommandFacade {
    @Override
    @PostMapping(UserAuthPathApiConstants.API_CHALLENGES)
    Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(
            @RequestBody IssueAuthChallengeApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGIN_MOBILE_OTP)
    Result<MobileOtpLoginApiCommandOutput> completeMobileOtpLogin(
            @RequestBody MobileOtpLoginApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGIN_PARTNER_TRUSTED_MOBILE)
    Result<ExternalLoginApiCommandOutput> loginWithTrustedPartnerMobile(
            @RequestBody TrustedPartnerMobileLoginApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_PROOF)
    Result<ExternalLoginApiCommandOutput> loginWithExternalProof(
            @RequestBody ExternalProofLoginApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL)
    Result<ExternalLoginApiCommandOutput> loginWithBoundExternalCredential(
            @RequestBody BoundExternalCredentialLoginApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_ATTEMPT)
    Result<ExternalAttemptLoginApiCommandOutput> attemptExternalLogin(
            @RequestBody ExternalAttemptLoginApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_COMPLETE)
    Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            @RequestBody ExternalLoginApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_CREDENTIALS_BIND)
    Result<Void> bindExternalCredential(
            @RequestBody BindExternalCredentialApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGIN_REFRESH)
    Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(
            @RequestBody RefreshTokenLoginApiCommand request
    );

    @Override
    @PostMapping(UserAuthPathApiConstants.API_LOGOUT)
    Result<LogoutApiCommandOutput> logout(
            @RequestBody LogoutApiCommand request
    );
}
