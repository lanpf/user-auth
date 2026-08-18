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
import com.cloud.userauth.application.challenge.IssueAuthChallengeOutput;
import com.cloud.userauth.application.login.MobileOtpLoginCommand;
import com.cloud.userauth.application.login.MobileOtpLoginOutput;
import com.cloud.userauth.application.login.external.TrustedMobileLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginOutput;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptCommand;
import com.cloud.userauth.application.login.external.ExternalLoginAttemptOutput;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginOutput;
import com.cloud.userauth.application.login.external.BoundCredentialLoginCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.application.credential.BindExternalCredentialCommand;
import com.cloud.userauth.application.logout.LogoutCommand;
import com.cloud.userauth.application.logout.LogoutOutput;

public interface AuthenticationApiMapper {
    IssueAuthChallengeCommand toCommand(IssueAuthChallengeApiCommand command);

    IssueAuthChallengeApiCommandOutput toOutput(IssueAuthChallengeOutput output);

    MobileOtpLoginCommand toCommand(MobileOtpLoginApiCommand command);

    MobileOtpLoginApiCommandOutput toOutput(MobileOtpLoginOutput output);

    RefreshTokenLoginCommand toCommand(RefreshTokenLoginApiCommand command);

    RefreshTokenLoginApiCommandOutput toOutput(RefreshTokenLoginOutput output);

    ExternalLoginAttemptCommand toCommand(ExternalLoginAttemptApiCommand command);

    ExternalLoginAttemptApiCommandOutput toOutput(ExternalLoginAttemptOutput output);

    ExternalLoginCommand toCommand(ExternalLoginApiCommand command);

    ExternalLoginApiCommandOutput toOutput(ExternalLoginOutput output);

    TrustedMobileLoginCommand toCommand(TrustedMobileLoginApiCommand command);

    BoundCredentialLoginCommand toCommand(BoundCredentialLoginApiCommand command);

    BindExternalCredentialCommand toCommand(
            BindExternalCredentialApiCommand command);

    LogoutCommand toCommand(LogoutApiCommand command);

    LogoutApiCommandOutput toOutput(LogoutOutput output);
}
