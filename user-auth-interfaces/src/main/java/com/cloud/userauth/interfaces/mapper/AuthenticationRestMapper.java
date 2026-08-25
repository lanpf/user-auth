package com.cloud.userauth.interfaces.mapper;

import com.cloud.framework.core.AuthenticatedSessionClientRequest;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import com.cloud.userauth.api.authentication.BoundExternalCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalProofLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedPartnerMobileLoginApiCommand;
import com.cloud.userauth.interfaces.rest.UserAuthenticationController;

public interface AuthenticationRestMapper {
    IssueAuthChallengeApiCommand toCommand(UserAuthenticationController.IssueAuthChallengeRequest request);

    MobileOtpLoginApiCommand toCommand(UserAuthenticationController.MobileOtpLoginRequest request);

    TrustedPartnerMobileLoginApiCommand toCommand(
            UserAuthenticationController.TrustedPartnerMobileLoginRequest request);

    ExternalProofLoginApiCommand toCommand(
            UserAuthenticationController.ExternalProofLoginRequest request);

    BoundExternalCredentialLoginApiCommand toCommand(
            UserAuthenticationController.BoundExternalCredentialLoginRequest request);

    ExternalAttemptLoginApiCommand toCommand(
            UserAuthenticationController.ExternalAttemptLoginRequest request);

    ExternalLoginApiCommand toCommand(UserAuthenticationController.ExternalLoginRequest request);

    BindExternalCredentialApiCommand toCommand(
            UserAuthenticationController.BindExternalCredentialRequest request);

    RefreshTokenLoginApiCommand toCommand(UserAuthenticationController.RefreshTokenLoginRequest request);

    LogoutApiCommand toLogoutCommand(AuthenticatedSessionClientRequest request);
}
