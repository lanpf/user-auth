package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authentication.BoundCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedMobileLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.application.challenge.IssueAuthChallengeCommand;
import com.cloud.userauth.application.challenge.IssueAuthChallengeCommandOutput;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginCommandOutput;
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
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.application.credential.BindExternalCredentialCommand;
import com.cloud.userauth.application.logout.LogoutCommand;
import com.cloud.userauth.application.logout.LogoutCommandOutput;

public interface AuthApiMapper {
    IssueAuthChallengeCommand toCommand(IssueAuthChallengeApiCommand request);

    IssueAuthChallengeApiCommandOutput toOutput(IssueAuthChallengeCommandOutput output);

    MobileOtpLoginCommand toCommand(MobileOtpLoginApiCommand request);

    MobileOtpLoginApiCommandOutput toOutput(MobileOtpLoginCommandOutput output);

    RefreshLoginCommand toCommand(RefreshTokenLoginApiCommand request);

    RefreshTokenLoginApiCommandOutput toOutput(RefreshLoginCommandOutput output);

    ExternalLoginAttemptCommand toCommand(ExternalLoginAttemptApiCommand request);

    ExternalLoginAttemptApiCommandOutput toOutput(ExternalLoginAttemptCommandOutput output);

    ExternalLoginCommand toCommand(ExternalLoginApiCommand request);

    ExternalLoginApiCommandOutput toOutput(ExternalLoginCommandOutput output);

    TrustedMobileAuthorizationCodeLoginCommand toCommand(TrustedMobileLoginApiCommand request);

    BoundExternalCredentialAuthorizationCodeLoginCommand toCommand(BoundCredentialLoginApiCommand request);

    BindExternalCredentialCommand toCommand(
            BindExternalCredentialApiCommand request);

    LogoutCommand toCommand(LogoutApiCommand request);

    LogoutApiCommandOutput toOutput(LogoutCommandOutput output);
}
