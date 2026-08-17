package com.cloud.userauth.interfaces.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.BoundCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.TrustedMobileLoginApiCommand;
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import com.cloud.userauth.application.challenge.AuthChallengeCommandService;
import com.cloud.userauth.application.login.MobileOtpLoginCommandService;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommandService;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommandService;
import com.cloud.userauth.application.login.external.ExternalLoginCommandService;
import com.cloud.userauth.application.login.external.TrustedMobileLoginCommandService;
import com.cloud.userauth.application.login.external.BoundCredentialLoginCommandService;
import com.cloud.userauth.interfaces.mapper.AuthApiMapper;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.application.credential.BindExternalCredentialCommandService;
import com.cloud.userauth.application.logout.LogoutCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class DefaultUserAuthenticationCommandFacade implements UserAuthenticationCommandFacade {
    private final AuthChallengeCommandService authChallengeCommandService;
    private final MobileOtpLoginCommandService mobileOtpLoginCommandService;
    private final RefreshTokenLoginCommandService refreshTokenLoginCommandService;
    private final ExternalLoginAttemptCommandService externalLoginAttemptCommandService;
    private final ExternalLoginCommandService externalLoginCommandService;
    private final TrustedMobileLoginCommandService trustedMobileLoginCommandService;
    private final BoundCredentialLoginCommandService boundCredentialLoginCommandService;
    private final LogoutCommandService logoutCommandService;
    private final BindExternalCredentialCommandService bindExternalCredentialCommandService;
    private final AuthApiMapper mapper;

    @Override
    public Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(IssueAuthChallengeApiCommand request) {
        return Result.success(mapper.toOutput(authChallengeCommandService.execute(mapper.toCommand(request))));
    }

    @Override
    public Result<MobileOtpLoginApiCommandOutput> loginWithMobileOtp(MobileOtpLoginApiCommand request) {
        return Result.success(mapper.toOutput(
                mobileOtpLoginCommandService.execute(mapper.toCommand(request))));
    }

    @Override
    public Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(RefreshTokenLoginApiCommand request) {
        return Result.success(mapper.toOutput(
                refreshTokenLoginCommandService.execute(mapper.toCommand(request))));
    }

    @Override
    public Result<ExternalLoginAttemptApiCommandOutput> createExternalLoginAttempt(
            ExternalLoginAttemptApiCommand request
    ) {
        return Result.success(mapper.toOutput(
                externalLoginAttemptCommandService.execute(mapper.toCommand(request))));
    }

    @Override
    public Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            ExternalLoginApiCommand request
    ) {
        return Result.success(mapper.toOutput(
                externalLoginCommandService.execute(mapper.toCommand(request))));
    }

    @Override
    public Result<ExternalLoginApiCommandOutput> loginWithTrustedMobile(
            TrustedMobileLoginApiCommand request
    ) {
        return Result.success(mapper.toOutput(
                trustedMobileLoginCommandService.execute(mapper.toCommand(request))));
    }

    @Override
    public Result<ExternalLoginApiCommandOutput> loginWithBoundCredential(
            BoundCredentialLoginApiCommand request
    ) {
        return Result.success(mapper.toOutput(
                boundCredentialLoginCommandService.execute(mapper.toCommand(request))));
    }


    @Override
    public Result<Void> bindExternalCredential(
            BindExternalCredentialApiCommand request
    ) {
        bindExternalCredentialCommandService.execute(mapper.toCommand(request));
        return Result.success();
    }

    @Override
    public Result<LogoutApiCommandOutput> logout(LogoutApiCommand request) {
        return Result.success(mapper.toOutput(logoutCommandService.execute(mapper.toCommand(request))));
    }

}
