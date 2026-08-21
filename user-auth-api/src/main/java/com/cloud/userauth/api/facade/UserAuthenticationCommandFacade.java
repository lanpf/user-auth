package com.cloud.userauth.api.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalProofLoginApiCommand;
import com.cloud.userauth.api.authentication.TrustedPartnerMobileLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.BoundExternalCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import jakarta.validation.Valid;

public interface UserAuthenticationCommandFacade {
    Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(@Valid IssueAuthChallengeApiCommand command);

    Result<MobileOtpLoginApiCommandOutput> completeMobileOtpLogin(@Valid MobileOtpLoginApiCommand command);

    Result<ExternalLoginApiCommandOutput> loginWithTrustedPartnerMobile(
            @Valid TrustedPartnerMobileLoginApiCommand command);

    Result<ExternalLoginApiCommandOutput> loginWithExternalProof(
            @Valid ExternalProofLoginApiCommand command);

    Result<ExternalLoginApiCommandOutput> loginWithBoundExternalCredential(
            @Valid BoundExternalCredentialLoginApiCommand command);

    Result<ExternalAttemptLoginApiCommandOutput> attemptExternalLogin(
            @Valid ExternalAttemptLoginApiCommand command);

    Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            @Valid ExternalLoginApiCommand command);

    Result<Void> bindExternalCredential(
            @Valid BindExternalCredentialApiCommand command);

    Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(@Valid RefreshTokenLoginApiCommand command);

    Result<LogoutApiCommandOutput> logout(@Valid LogoutApiCommand command);

}
