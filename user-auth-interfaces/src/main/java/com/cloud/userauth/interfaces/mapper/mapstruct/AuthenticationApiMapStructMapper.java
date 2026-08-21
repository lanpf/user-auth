package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.BoundExternalCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalProofLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedPartnerMobileLoginApiCommand;
import com.cloud.userauth.application.challenge.IssueAuthChallengeCommand;
import com.cloud.userauth.application.challenge.IssueAuthChallengeOutput;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalAttemptLoginCommand;
import com.cloud.userauth.application.login.external.ExternalAttemptLoginOutput;
import com.cloud.userauth.application.login.external.ExternalProofLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginOutput;
import com.cloud.userauth.application.login.external.TrustedPartnerMobileLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.BoundExternalCredentialLoginCommand;
import com.cloud.userauth.interfaces.mapper.AuthenticationApiMapper;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.application.credential.BindExternalCredentialCommand;
import com.cloud.userauth.application.logout.LogoutCommand;
import com.cloud.userauth.application.logout.LogoutOutput;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface AuthenticationApiMapStructMapper extends AuthenticationApiMapper {
    @Override
    IssueAuthChallengeCommand toCommand(IssueAuthChallengeApiCommand request);

    @Override
    IssueAuthChallengeApiCommandOutput toOutput(IssueAuthChallengeOutput output);

    @Override
    MobileOtpLoginCommand toCommand(MobileOtpLoginApiCommand request);

    @Override
    MobileOtpLoginApiCommandOutput toOutput(MobileOtpLoginOutput output);

    @Override
    RefreshTokenLoginCommand toCommand(RefreshTokenLoginApiCommand request);

    @Override
    RefreshTokenLoginApiCommandOutput toOutput(RefreshTokenLoginOutput output);

    @Override
    ExternalAttemptLoginCommand toCommand(ExternalAttemptLoginApiCommand request);

    @Override
    ExternalAttemptLoginApiCommandOutput toOutput(ExternalAttemptLoginOutput output);

    @Override
    ExternalProofLoginCommand toCommand(ExternalProofLoginApiCommand request);

    @Override
    ExternalLoginCommand toCommand(ExternalLoginApiCommand request);

    @Override
    ExternalLoginApiCommandOutput toOutput(ExternalLoginOutput output);

    @Override
    TrustedPartnerMobileLoginCommand toCommand(TrustedPartnerMobileLoginApiCommand request);

    @Override
    BoundExternalCredentialLoginCommand toCommand(BoundExternalCredentialLoginApiCommand request);

    @Override
    BindExternalCredentialCommand toCommand(
            BindExternalCredentialApiCommand request);

    @Override
    LogoutCommand toCommand(LogoutApiCommand request);

    @Override
    LogoutApiCommandOutput toOutput(LogoutOutput output);
}
