package com.cloud.userauth.client.openfeign;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-auth", contextId = "userAuthenticationCommandFeignClient", path = "/api/user-auth")
public interface UserAuthenticationCommandFeignClient extends UserAuthenticationCommandFacade {
    @Override
    @PostMapping("/auth-challenges")
    Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(
            @RequestBody IssueAuthChallengeApiCommand request
    );

    @Override
    @PostMapping("/login/mobile-otp")
    Result<MobileOtpLoginApiCommandOutput> loginWithMobileOtp(
            @RequestBody MobileOtpLoginApiCommand request
    );

    @Override
    @PostMapping("/login/refresh")
    Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(
            @RequestBody RefreshTokenLoginApiCommand request
    );
}
