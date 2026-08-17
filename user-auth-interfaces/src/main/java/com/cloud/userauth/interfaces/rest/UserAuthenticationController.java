package com.cloud.userauth.interfaces.rest;

import com.cloud.framework.core.ClientChannelRequest;
import com.cloud.framework.core.ClientRequest;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.ExternalLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.ExternalLoginAttemptApiCommandOutput;
import com.cloud.userauth.api.authentication.IssueAuthChallengeApiCommandOutput;
import com.cloud.userauth.api.authentication.LogoutApiCommandOutput;
import com.cloud.userauth.api.authentication.MobileOtpLoginApiCommandOutput;
import com.cloud.userauth.api.authentication.RefreshTokenLoginApiCommandOutput;
import com.cloud.userauth.api.enums.AuthChallengeSceneApiEnum;
import com.cloud.userauth.api.enums.AuthChallengeTypeApiEnum;
import com.cloud.userauth.api.enums.ExternalProofTypeApiEnum;
import com.cloud.userauth.api.facade.UserAuthenticationCommandFacade;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.interfaces.mapper.AuthenticationRestMapper;
import com.cloud.userauth.interfaces.security.AuthenticatedSessionResolver;
import com.cloud.userauth.interfaces.security.H5SessionCookie;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserAuthenticationController {
    private final UserAuthenticationCommandFacade facade;
    private final AuthenticationRestMapper mapper;

    @PostMapping(UserAuthRestPaths.API_AUTH_CHALLENGES)
    public Result<IssueAuthChallengeApiCommandOutput> issueAuthChallenge(
            @Valid @RequestBody IssueAuthChallengeRequest request
    ) {
        return facade.issueAuthChallenge(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_LOGIN_MOBILE_OTP)
    public Result<MobileOtpLoginApiCommandOutput> loginWithMobileOtp(
            @Valid @RequestBody MobileOtpLoginRequest request
    ) {
        return facade.loginWithMobileOtp(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_LOGIN_EXTERNAL_ATTEMPTS)
    public Result<ExternalLoginAttemptApiCommandOutput> createExternalLoginAttempt(
            @Valid @RequestBody ExternalLoginAttemptRequest request
    ) {
        return facade.createExternalLoginAttempt(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_LOGIN_EXTERNAL)
    public Result<ExternalLoginApiCommandOutput> completeExternalLogin(
            @Valid @RequestBody ExternalLoginRequest request
    ) {
        return facade.completeExternalLogin(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_LOGIN_EXTERNAL_TRUSTED_MOBILE)
    public Result<ExternalLoginApiCommandOutput> loginWithTrustedMobile(
            @Valid @RequestBody UserAuthenticationController.TrustedMobileLoginRequest request
    ) {
        return facade.loginWithTrustedMobile(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_LOGIN_EXTERNAL_BOUND_CREDENTIAL)
    public Result<ExternalLoginApiCommandOutput> loginWithBoundCredential(
            @Valid @RequestBody UserAuthenticationController.BoundCredentialLoginRequest request
    ) {
        return facade.loginWithBoundCredential(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_CREDENTIALS_EXTERNAL_BIND)
    public Result<Void> bindExternalCredential(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody BindExternalCredentialRequest request
    ) {
        AuthenticatedSession authenticated = AuthenticatedSessionResolver.resolve(principal);
        return facade.bindExternalCredential(mapper.toBindExternalCredentialCommand(
                request, authenticated.userId(), authenticated.authAccountId()));
    }

    @PostMapping(UserAuthRestPaths.API_LOGIN_REFRESH)
    public Result<RefreshTokenLoginApiCommandOutput> refreshTokenLogin(
            @Valid @RequestBody UserAuthenticationController.RefreshTokenLoginRequest request
    ) {
        return facade.refreshTokenLogin(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_LOGOUT)
    public Result<LogoutApiCommandOutput> logout(
            @AuthenticationPrincipal Object principal,
            HttpServletResponse response
    ) {
        AuthenticatedSession authenticated = AuthenticatedSessionResolver.resolve(principal);
        Result<LogoutApiCommandOutput> result = facade.logout(
                mapper.toLogoutCommand(authenticated.sessionId().value()));
        H5SessionCookie.clear(response);
        return result;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BindExternalCredentialRequest extends ClientRequest {
        @NotBlank
        private String issuer;
        @NotBlank
        private String authorizationCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class IssueAuthChallengeRequest extends ClientChannelRequest {
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
    public abstract static class LoginRequest extends ClientChannelRequest {
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
    public static class TrustedMobileLoginRequest extends LoginRequest {
        @NotBlank
        private String issuer;
        @NotBlank
        private String authorizationCode;
        @NotBlank
        private String mobile;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BoundCredentialLoginRequest extends LoginRequest {
        @NotBlank
        private String issuer;
        @NotBlank
        private String authorizationCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExternalLoginAttemptRequest extends ClientChannelRequest {
        @NotBlank
        private String issuer;
        @NotNull
        private ExternalProofTypeApiEnum proofType;
        @NotEmpty
        private Map<@NotBlank String, String> proofParameters;
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
