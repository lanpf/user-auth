package com.cloud.userauth.interfaces.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.BoundExternalCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalProofLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.TrustedPartnerMobileLoginApiCommand;
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import com.cloud.userauth.application.challenge.AuthChallengeIssueCommandService;
import com.cloud.userauth.application.login.MobileOtpLoginCommandService;
import com.cloud.userauth.application.login.external.ExternalProofLoginCommandService;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommandService;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalAttemptLoginCommandService;
import com.cloud.userauth.application.login.external.ExternalLoginCommandService;
import com.cloud.userauth.application.login.external.TrustedPartnerMobileLoginCommandService;
import com.cloud.userauth.application.login.external.BoundExternalCredentialLoginCommandService;
import com.cloud.userauth.interfaces.mapper.AuthenticationApiMapper;
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
    private final AuthChallengeIssueCommandService authChallengeIssueCommandService;
    private final MobileOtpLoginCommandService mobileOtpLoginCommandService;
    private final TrustedPartnerMobileLoginCommandService trustedPartnerMobileLoginCommandService;
    private final ExternalProofLoginCommandService externalProofLoginCommandService;
    private final BoundExternalCredentialLoginCommandService boundExternalCredentialLoginCommandService;
    private final ExternalAttemptLoginCommandService externalAttemptLoginCommandService;
    private final ExternalLoginCommandService externalLoginCommandService;
    private final BindExternalCredentialCommandService bindExternalCredentialCommandService;
    private final LogoutCommandService logoutCommandService;
    private final RefreshTokenLoginCommandService refreshTokenLoginCommandService;
    private final AuthenticationApiMapper mapper;

    @Override
    public Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(IssueAuthChallengeApiCommand command) {
        return Result.success(mapper.toOutput(authChallengeIssueCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<MobileOtpLoginApiCommandOutput> completeMobileOtpLogin(MobileOtpLoginApiCommand command) {
        return Result.success(mapper.toOutput(
                mobileOtpLoginCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<ExternalLoginApiCommandOutput> loginWithTrustedPartnerMobile(
            TrustedPartnerMobileLoginApiCommand command
    ) {
        return Result.success(mapper.toOutput(
                trustedPartnerMobileLoginCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<ExternalLoginApiCommandOutput> loginWithExternalProof(
            ExternalProofLoginApiCommand command) {
        return Result.success(mapper.toOutput(
                externalProofLoginCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<ExternalLoginApiCommandOutput> loginWithBoundExternalCredential(
            BoundExternalCredentialLoginApiCommand command
    ) {
        return Result.success(mapper.toOutput(
                boundExternalCredentialLoginCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<ExternalAttemptLoginApiCommandOutput> attemptExternalLogin(
            ExternalAttemptLoginApiCommand command
    ) {
        return Result.success(mapper.toOutput(
                externalAttemptLoginCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            ExternalLoginApiCommand command
    ) {
        return Result.success(mapper.toOutput(
                externalLoginCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<Void> bindExternalCredential(
            BindExternalCredentialApiCommand command
    ) {
        bindExternalCredentialCommandService.execute(mapper.toCommand(command));
        return Result.success();
    }

    @Override
    public Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(RefreshTokenLoginApiCommand command) {
        return Result.success(mapper.toOutput(
                refreshTokenLoginCommandService.execute(mapper.toCommand(command))));
    }

    @Override
    public Result<LogoutApiCommandOutput> logout(LogoutApiCommand command) {
        return Result.success(mapper.toOutput(logoutCommandService.execute(mapper.toCommand(command))));
    }

}
