package com.cloud.userauth.application.login.refresh;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import com.cloud.userauth.application.port.ClientRenewalPolicyResolver;
import com.cloud.userauth.application.port.LoginTokenRefresher;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@RequiredArgsConstructor
public class RefreshLoginCommandService {
    private final ClientRenewalPolicyResolver renewalPolicyResolver;
    private final LoginTokenRefresher loginTokenRefresher;

    public RefreshLoginCommandOutput execute(RefreshLoginCommand command) {
        if (renewalPolicyResolver.resolve(command.clientAppId()) != ClientRenewalPolicy.REFRESH_TOKEN_ROTATION) {
            throw new ApplicationException(ApplicationError.APP_CLIENT_RENEWAL_POLICY_NOT_ALLOWED);
        }
        return loginTokenRefresher.refresh(command);
    }
}
