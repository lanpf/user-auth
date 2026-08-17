package com.cloud.userauth.application.login.refresh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.ClientRenewalPolicy;
import org.junit.jupiter.api.Test;

class RefreshLoginCommandServiceTest {
    @Test
    void shouldRefreshWhenClientPolicyUsesRotation() {
        RefreshLoginCommandOutput expected = new RefreshLoginCommandOutput(
                "Bearer", "access-2", "refresh-2", 900L, "app.api",
                1L, 2L, "session-1");
        RefreshLoginCommandService service = new RefreshLoginCommandService(
                clientAppId -> ClientRenewalPolicy.REFRESH_TOKEN_ROTATION,
                command -> expected);

        RefreshLoginCommandOutput actual =
                service.execute(new RefreshLoginCommand("app", "refresh-1"));

        assertEquals(expected, actual);
    }

    @Test
    void shouldRejectRefreshForAuthorizationCodeClient() {
        RefreshLoginCommandService service = new RefreshLoginCommandService(
                clientAppId -> ClientRenewalPolicy.EXTERNAL_AUTHORIZATION_CODE,
                command -> { throw new AssertionError("refresher must not be invoked"); });

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> service.execute(new RefreshLoginCommand("mini-program", "refresh-1")));

        assertEquals(ApplicationError.APP_CLIENT_RENEWAL_POLICY_NOT_ALLOWED.errorCode(),
                exception.getErrorCode());
    }
}
