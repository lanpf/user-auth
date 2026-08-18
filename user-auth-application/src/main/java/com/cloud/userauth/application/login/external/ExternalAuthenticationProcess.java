package com.cloud.userauth.application.login.external;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.authorization.UserChannelAuthorizationSynchronizer;
import com.cloud.userauth.application.port.UserGateway;
import com.cloud.userauth.domain.user.UserId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExternalAuthenticationProcess {
    private final ExternalLoginTransactionService transactionService;
    private final UserChannelAuthorizationSynchronizer userChannelAuthorizationSynchronizer;
    private final UserGateway userGateway;

    public ExternalAuthenticationOutput authenticate(ExternalAuthenticationCommand command) {
        ExternalAccountPreparationOutput prepared = transactionService.prepareAccount(command);
        try {
            userGateway.initializeUser(prepared.userId());
        } catch (RuntimeException exception) {
            throw new ApplicationException(
                    ApplicationError.APP_USER_INITIALIZATION_FAILED,
                    exception);
        }
        ExternalAuthenticationOutput authenticated =
                transactionService.completeLogin(command, prepared);
        userChannelAuthorizationSynchronizer.synchronize(
                new UserId(authenticated.userId()), command.channelCode());
        return authenticated;
    }
}
