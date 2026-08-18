package com.cloud.userauth.application.login;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.authorization.UserChannelAuthorizationSynchronizer;
import com.cloud.userauth.application.port.MobileOtpLoginLock;
import com.cloud.userauth.application.port.UserGateway;
import com.cloud.userauth.application.registration.RegistrationProcess;
import com.cloud.userauth.application.registration.RegistrationProcessId;
import com.cloud.userauth.application.registration.RegistrationProcessRepository;
import com.cloud.userauth.domain.authentication.account.AuthAccount;
import com.cloud.userauth.domain.authentication.account.AuthAccountRepository;
import com.cloud.userauth.domain.authentication.challenge.AuthChallenge;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeRepository;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeStatus;
import com.cloud.userauth.domain.authentication.challenge.ChallengeConsumerType;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authentication.credential.CredentialKey;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * 手机号登录的跨边界流程编排。
 * 负责挑战验证、账号登录/注册、user 初始化的完整编排，不处理 Token 签发。
 */
@Validated
@RequiredArgsConstructor
public class MobileOtpAuthenticationProcess {
    private final AuthChallengeRepository challengeRepository;
    private final RegistrationProcessRepository registrationRepository;
    private final AuthAccountRepository authAccountRepository;
    private final MobileOtpLoginLock mobileOtpLoginLock;
    private final MobileOtpLoginTransactionService mobileOtpLoginTransactionService;
    private final UserChannelAuthorizationSynchronizer userChannelAuthorizationSynchronizer;
    private final UserGateway userGateway;

    public MobileOtpAuthenticationOutput authenticate(@Valid MobileOtpAuthenticationCommand command) {
        AuthChallenge initialChallenge = challengeRepository.findById(new AuthChallengeId(command.challengeId()))
                .orElseThrow(() -> new DomainException(DomainError.AUTH_CHALLENGE_NOT_FOUND));
        LoginMobile mobile = new LoginMobile(initialChallenge.getTarget().value());
        MobileOtpAuthenticationOutput authenticated =
                mobileOtpLoginLock.execute(mobile, () -> authenticateLocked(command));
        userChannelAuthorizationSynchronizer.synchronize(
                new UserId(authenticated.userId()), command.channelCode());
        return authenticated;
    }

    private MobileOtpAuthenticationOutput authenticateLocked(MobileOtpAuthenticationCommand command) {
        AuthChallenge challenge = mobileOtpLoginTransactionService.verifyChallenge(command);
        if (challenge.getStatus() == AuthChallengeStatus.CONSUMED) {
            return resumeConsumed(challenge, command);
        }

        LoginMobile mobile = new LoginMobile(challenge.getTarget().value());
        AuthAccount account = authAccountRepository.findByCredential(CredentialKey.mobile(mobile)).orElse(null);
        if (account != null) {
            return mobileOtpLoginTransactionService.loginExistingAccount(account.id(), challenge.id(), command);
        }
        RegistrationProcess process = mobileOtpLoginTransactionService.startRegistration(challenge.id(), mobile);
        return resumeRegistration(process, command);
    }

    private MobileOtpAuthenticationOutput resumeConsumed(
            AuthChallenge challenge,
            MobileOtpAuthenticationCommand command
    ) {
        if (challenge.getConsumedByType() == ChallengeConsumerType.REGISTRATION_PROCESS) {
            RegistrationProcess process = registrationRepository.findById(
                            new RegistrationProcessId(Long.valueOf(challenge.getConsumedById())))
                    .orElseThrow(() -> new ApplicationException(ApplicationError.APP_REGISTRATION_PROCESS_NOT_FOUND));
            return resumeRegistration(process, command);
        }
        if (challenge.getConsumedByType() == ChallengeConsumerType.LOGIN_SESSION) {
            return mobileOtpLoginTransactionService.replayLogin(new SessionId(challenge.getConsumedById()), false);
        }
        throw new DomainException(DomainError.AUTH_CHALLENGE_CONSUMED);
    }

    private MobileOtpAuthenticationOutput resumeRegistration(
            RegistrationProcess process,
            MobileOtpAuthenticationCommand command
    ) {
        if (process.isCompleted()) {
            return mobileOtpLoginTransactionService.replayLogin(process.getSessionId(), true);
        }
        if (process.needsUserInitialization()) {
            try {
                userGateway.initializeUser(process.getUserId());
            } catch (RuntimeException exception) {
                mobileOtpLoginTransactionService.markRetryableFailure(process.id(), exception.getMessage());
                throw new ApplicationException(ApplicationError.APP_USER_INITIALIZATION_FAILED, exception);
            }
            mobileOtpLoginTransactionService.markUserInitialized(process.id());
        }
        return mobileOtpLoginTransactionService.completeRegistration(process.id(), command);
    }
}
