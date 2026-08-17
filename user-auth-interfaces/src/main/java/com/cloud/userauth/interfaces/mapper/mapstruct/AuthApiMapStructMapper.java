package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.BoundCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedMobileLoginApiCommand;
import com.cloud.userauth.application.challenge.IssueAuthChallengeCommand;
import com.cloud.userauth.application.challenge.IssueAuthChallengeCommandOutput;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.application.login.refresh.RefreshLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshLoginCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommand;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginCommandOutput;
import com.cloud.userauth.application.login.external.TrustedMobileAuthorizationCodeLoginCommand;
import com.cloud.userauth.application.login.external.BoundExternalCredentialAuthorizationCodeLoginCommand;
import com.cloud.userauth.interfaces.mapper.AuthApiMapper;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.application.credential.BindExternalCredentialCommand;
import com.cloud.userauth.application.logout.LogoutCommand;
import com.cloud.userauth.application.logout.LogoutCommandOutput;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface AuthApiMapStructMapper extends AuthApiMapper {
    @Override
    IssueAuthChallengeCommand toCommand(IssueAuthChallengeApiCommand request);

    @Override
    IssueAuthChallengeApiCommandOutput toOutput(IssueAuthChallengeCommandOutput output);

    @Override
    MobileOtpLoginCommand toCommand(MobileOtpLoginApiCommand request);

    @Override
    MobileOtpLoginApiCommandOutput toOutput(MobileOtpLoginCommandOutput output);

    @Override
    RefreshLoginCommand toCommand(RefreshTokenLoginApiCommand request);

    @Override
    RefreshTokenLoginApiCommandOutput toOutput(RefreshLoginCommandOutput output);

    @Override
    ExternalLoginAttemptCommand toCommand(ExternalLoginAttemptApiCommand request);

    @Override
    ExternalLoginAttemptApiCommandOutput toOutput(ExternalLoginAttemptCommandOutput output);

    @Override
    ExternalLoginCommand toCommand(ExternalLoginApiCommand request);

    @Override
    ExternalLoginApiCommandOutput toOutput(ExternalLoginCommandOutput output);

    @Override
    TrustedMobileAuthorizationCodeLoginCommand toCommand(TrustedMobileLoginApiCommand request);

    @Override
    BoundExternalCredentialAuthorizationCodeLoginCommand toCommand(BoundCredentialLoginApiCommand request);

    @Override
    BindExternalCredentialCommand toCommand(
            BindExternalCredentialApiCommand request);

    @Override
    LogoutCommand toCommand(LogoutApiCommand request);

    @Override
    LogoutApiCommandOutput toOutput(LogoutCommandOutput output);
}
