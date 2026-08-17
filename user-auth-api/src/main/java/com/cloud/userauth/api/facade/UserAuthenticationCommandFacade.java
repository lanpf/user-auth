package com.cloud.userauth.api.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.TrustedMobileLoginApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommand;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommand;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommand;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommand;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.BoundCredentialLoginApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommand;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.BindExternalCredentialApiCommand;
import jakarta.validation.Valid;

public interface UserAuthenticationCommandFacade {
    Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(@Valid IssueAuthChallengeApiCommand command);

    Result<MobileOtpLoginApiCommandOutput> loginWithMobileOtp(@Valid MobileOtpLoginApiCommand command);

    Result<ExternalLoginAttemptApiCommandOutput> createExternalLoginAttempt(
            @Valid ExternalLoginAttemptApiCommand command);

    Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            @Valid ExternalLoginApiCommand command);

    Result<ExternalLoginApiCommandOutput> loginWithTrustedMobile(
            @Valid TrustedMobileLoginApiCommand command);

    Result<ExternalLoginApiCommandOutput> loginWithBoundCredential(
            @Valid BoundCredentialLoginApiCommand command);

    Result<Void> bindExternalCredential(
            @Valid BindExternalCredentialApiCommand command);

    Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(@Valid RefreshTokenLoginApiCommand command);

    Result<LogoutApiCommandOutput> logout(@Valid LogoutApiCommand command);

}
