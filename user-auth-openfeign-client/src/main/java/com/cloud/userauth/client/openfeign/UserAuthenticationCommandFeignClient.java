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
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-auth", contextId = "userAuthenticationCommandFeignClient", path = "/api/user-auth")
public interface UserAuthenticationCommandFeignClient extends UserAuthenticationCommandFacade {
    @Override
    @PostMapping("/challenges")
    Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(
            @RequestBody IssueAuthChallengeApiCommand request
    );

    @Override
    @PostMapping("/login/mobile-otp")
    Result<MobileOtpLoginApiCommandOutput> completeMobileOtpLogin(
            @RequestBody MobileOtpLoginApiCommand request
    );

    @Override
    @PostMapping("/login/partner/trusted-mobile")
    Result<ExternalLoginApiCommandOutput> loginWithTrustedPartnerMobile(
            @RequestBody TrustedPartnerMobileLoginApiCommand request
    );

    @Override
    @PostMapping("/login/external/proof")
    Result<ExternalLoginApiCommandOutput> loginWithExternalProof(
            @RequestBody ExternalProofLoginApiCommand request
    );

    @Override
    @PostMapping("/login/external/bound-credential")
    Result<ExternalLoginApiCommandOutput> loginWithBoundExternalCredential(
            @RequestBody BoundExternalCredentialLoginApiCommand request
    );

    @Override
    @PostMapping("/login/external/attempt")
    Result<ExternalAttemptLoginApiCommandOutput> attemptExternalLogin(
            @RequestBody ExternalAttemptLoginApiCommand request
    );

    @Override
    @PostMapping("/login/external/complete")
    Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            @RequestBody ExternalLoginApiCommand request
    );

    @Override
    @PostMapping("/credentials/bind")
    Result<Void> bindExternalCredential(
            @RequestBody BindExternalCredentialApiCommand request
    );

    @Override
    @PostMapping("/login/refresh")
    Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(
            @RequestBody RefreshTokenLoginApiCommand request
    );

    @Override
    @PostMapping("/logout")
    Result<LogoutApiCommandOutput> logout(
            @RequestBody LogoutApiCommand request
    );
}
