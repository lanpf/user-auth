package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authentication.BoundExternalCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalProofLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedPartnerMobileLoginApiCommand;
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
import com.cloud.userauth.application.login.external.ExternalAttemptLoginCommand;
import com.cloud.userauth.application.login.external.ExternalAttemptLoginOutput;
import com.cloud.userauth.application.login.external.ExternalProofLoginCommand;
import com.cloud.userauth.application.login.external.TrustedPartnerMobileLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginOutput;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.application.login.external.ExternalLoginCommand;
import com.cloud.userauth.application.login.external.ExternalLoginOutput;
import com.cloud.userauth.application.login.external.BoundExternalCredentialLoginCommand;
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

    ExternalAttemptLoginCommand toCommand(ExternalAttemptLoginApiCommand command);

    ExternalAttemptLoginApiCommandOutput toOutput(ExternalAttemptLoginOutput output);

    ExternalProofLoginCommand toCommand(ExternalProofLoginApiCommand command);

    ExternalLoginCommand toCommand(ExternalLoginApiCommand command);

    ExternalLoginApiCommandOutput toOutput(ExternalLoginOutput output);

    TrustedPartnerMobileLoginCommand toCommand(TrustedPartnerMobileLoginApiCommand command);

    BoundExternalCredentialLoginCommand toCommand(BoundExternalCredentialLoginApiCommand command);

    BindExternalCredentialCommand toCommand(
            BindExternalCredentialApiCommand command);

    LogoutCommand toCommand(LogoutApiCommand command);

    LogoutApiCommandOutput toOutput(LogoutOutput output);
}
