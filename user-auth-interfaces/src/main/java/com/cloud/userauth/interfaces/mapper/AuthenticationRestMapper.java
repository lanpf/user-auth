package com.cloud.userauth.interfaces.mapper;

import com.cloud.framework.core.AuthenticatedSessionRequest;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.api.authentication.BoundCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedMobileLoginApiCommand;
import com.cloud.userauth.interfaces.rest.UserAuthenticationController;

public interface AuthenticationRestMapper {
    IssueAuthChallengeApiCommand toCommand(UserAuthenticationController.IssueAuthChallengeRequest request);

    MobileOtpLoginApiCommand toCommand(UserAuthenticationController.MobileOtpLoginRequest request);

    RefreshTokenLoginApiCommand toCommand(UserAuthenticationController.RefreshTokenLoginRequest request);

    TrustedMobileLoginApiCommand toCommand(
            UserAuthenticationController.TrustedMobileLoginRequest request);

    BoundCredentialLoginApiCommand toCommand(
            UserAuthenticationController.BoundCredentialLoginRequest request);

    ExternalLoginAttemptApiCommand toCommand(
            UserAuthenticationController.ExternalLoginAttemptRequest request);

    ExternalLoginApiCommand toCommand(UserAuthenticationController.ExternalLoginRequest request);

    BindExternalCredentialApiCommand toCommand(
            UserAuthenticationController.BindExternalCredentialRequest request);

    LogoutApiCommand toLogoutCommand(AuthenticatedSessionRequest request);
}
