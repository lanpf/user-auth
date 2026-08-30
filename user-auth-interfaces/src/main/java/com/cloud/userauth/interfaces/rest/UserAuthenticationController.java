package com.cloud.userauth.interfaces.rest;

import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.framework.core.AuthenticatedSessionClientRequest;
import com.cloud.framework.core.ChannelClientRequest;
import com.cloud.framework.core.ClientRequest;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalAttemptLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.enums.AuthChallengeSceneApiEnum;
import com.cloud.userauth.api.enums.AuthChallengeTypeApiEnum;
import com.cloud.userauth.api.enums.ExternalProofTypeApiEnum;
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import com.cloud.userauth.interfaces.mapper.AuthenticationRestMapper;
import com.cloud.userauth.interfaces.security.BrowserSessionCookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserAuthenticationController {
    private final UserAuthenticationCommandFacade facade;
    private final AuthenticationRestMapper mapper;

    @PostMapping(UserAuthPathApiConstants.API_CHALLENGES)
    public Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(
            @Valid @RequestBody IssueAuthChallengeRequest request
    ) {
        return facade.issueAuthChallenge(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGIN_MOBILE_OTP)
    public Result<MobileOtpLoginApiCommandOutput> completeMobileOtpLogin(
            @Valid @RequestBody MobileOtpLoginRequest request
    ) {
        return facade.completeMobileOtpLogin(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGIN_PARTNER_TRUSTED_MOBILE)
    public Result<ExternalLoginApiCommandOutput> loginWithTrustedPartnerMobile(
            @Valid @RequestBody UserAuthenticationController.TrustedPartnerMobileLoginRequest request
    ) {
        return facade.loginWithTrustedPartnerMobile(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_PROOF)
    public Result<ExternalLoginApiCommandOutput> loginWithExternalProof(
            @Valid @RequestBody UserAuthenticationController.ExternalProofLoginRequest request
    ) {
        return facade.loginWithExternalProof(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL)
    public Result<ExternalLoginApiCommandOutput> loginWithBoundExternalCredential(
            @Valid @RequestBody UserAuthenticationController.BoundExternalCredentialLoginRequest request
    ) {
        return facade.loginWithBoundExternalCredential(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_ATTEMPT)
    public Result<ExternalAttemptLoginApiCommandOutput> attemptExternalLogin(
            @Valid @RequestBody UserAuthenticationController.ExternalAttemptLoginRequest request
    ) {
        return facade.attemptExternalLogin(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGIN_EXTERNAL_COMPLETE)
    public Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            @Valid @RequestBody ExternalLoginRequest request
    ) {
        return facade.completeExternalLogin(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_CREDENTIALS_BIND)
    public Result<Void> bindExternalCredential(
            @Valid @RequestBody BindExternalCredentialRequest request
    ) {
        return facade.bindExternalCredential(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGIN_REFRESH)
    public Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(
            @Valid @RequestBody UserAuthenticationController.RefreshTokenLoginRequest request
    ) {
        return facade.refreshTokenLogin(mapper.toCommand(request));
    }

    @PostMapping(UserAuthPathApiConstants.API_LOGOUT)
    public Result<LogoutApiCommandOutput> logout(
            @Valid AuthenticatedSessionClientRequest request,
            HttpServletResponse response
    ) {
        Result<LogoutApiCommandOutput> result = facade.logout(
                mapper.toLogoutCommand(request));
        BrowserSessionCookie.clear(response);
        return result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BindExternalCredentialRequest extends AuthenticatedSessionClientRequest {
        @NotBlank
        private String issuer;
        @NotBlank
        private String authorizationCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class IssueAuthChallengeRequest extends ChannelClientRequest {
        @NotNull
        private AuthChallengeTypeApiEnum challengeType;
        @NotBlank
        private String target;
        @NotNull
        private AuthChallengeSceneApiEnum scene;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public abstract static class LoginRequest extends ChannelClientRequest {
        private String deviceId;
        private String deviceType;
        private String deviceName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MobileOtpLoginRequest extends LoginRequest {
        @NotNull
        @Positive
        private Long challengeId;
        @NotBlank
        private String code;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RefreshTokenLoginRequest extends ClientRequest {
        @NotBlank
        private String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TrustedPartnerMobileLoginRequest extends LoginRequest {
        @NotBlank
        private String partnerCode;
        @NotBlank
        private String partnerBizId;
        @NotBlank
        private String mobile;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BoundExternalCredentialLoginRequest extends LoginRequest {
        @NotBlank
        private String issuer;
        @NotBlank
        private String authorizationCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExternalAttemptLoginRequest extends ChannelClientRequest {
        @NotBlank
        private String issuer;
        @NotNull
        private ExternalProofTypeApiEnum proofType;
        @NotEmpty
        private Map<@NotBlank String, @NotBlank String> proofParameters;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExternalProofLoginRequest extends LoginRequest {
        @NotBlank
        private String issuer;
        @NotNull
        private ExternalProofTypeApiEnum proofType;
        @NotEmpty
        private Map<@NotBlank String, @NotBlank String> proofParameters;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExternalLoginRequest extends LoginRequest {
        @NotBlank
        private String loginAttemptId;
        @Positive
        private Long challengeId;
        private String code;
    }
}
