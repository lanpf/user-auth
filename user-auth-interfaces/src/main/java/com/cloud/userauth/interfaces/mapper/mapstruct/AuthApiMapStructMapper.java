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
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommand;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginCommandOutput;
import com.cloud.userauth.application.login.external.TrustedMobileLoginCommand;
import com.cloud.userauth.application.login.external.BoundCredentialLoginCommand;
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
    RefreshTokenLoginCommand toCommand(RefreshTokenLoginApiCommand request);

    @Override
    RefreshTokenLoginApiCommandOutput toOutput(RefreshTokenLoginCommandOutput output);

    @Override
    ExternalLoginAttemptCommand toCommand(ExternalLoginAttemptApiCommand request);

    @Override
    ExternalLoginAttemptApiCommandOutput toOutput(ExternalLoginAttemptCommandOutput output);

    @Override
    ExternalLoginCommand toCommand(ExternalLoginApiCommand request);

    @Override
    ExternalLoginApiCommandOutput toOutput(ExternalLoginCommandOutput output);

    @Override
    TrustedMobileLoginCommand toCommand(TrustedMobileLoginApiCommand request);

    @Override
    BoundCredentialLoginCommand toCommand(BoundCredentialLoginApiCommand request);

    @Override
    BindExternalCredentialCommand toCommand(
            BindExternalCredentialApiCommand request);

    @Override
    LogoutCommand toCommand(LogoutApiCommand request);

    @Override
    LogoutApiCommandOutput toOutput(LogoutCommandOutput output);
}
