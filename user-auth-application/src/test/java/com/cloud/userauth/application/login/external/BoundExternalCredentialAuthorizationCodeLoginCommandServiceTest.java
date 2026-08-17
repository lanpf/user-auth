package com.cloud.userauth.application.login.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import org.junit.jupiter.api.Test;

class BoundExternalCredentialAuthorizationCodeLoginCommandServiceTest {
    @Test
    void shouldRejectExternalAuthorizationCodeWhenClientPolicyDoesNotAllowIt() {
        BoundExternalCredentialAuthorizationCodeLoginCommandService service =
                new BoundExternalCredentialAuthorizationCodeLoginCommandService(
                        null,
                        null,
                        clientAppId -> ClientRenewalPolicy.NONE);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> service.execute(new BoundExternalCredentialAuthorizationCodeLoginCommand(
                        "WECHAT_MINI_PROGRAM",
                        "authorization-code",
                        null,
                        null,
                        null,
                        "mini-program",
                        "MINI_PROGRAM",
                        "1.0",
                        "DEFAULT")));

        assertEquals(
                ApplicationError.APP_LOGIN_CLIENT_NOT_ALLOWED.errorCode(),
                exception.getErrorCode());
    }
}
