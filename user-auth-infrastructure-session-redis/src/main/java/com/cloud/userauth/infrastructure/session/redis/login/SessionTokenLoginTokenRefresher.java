package com.cloud.userauth.infrastructure.session.redis.login;

import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommandOutput;
import com.cloud.userauth.application.port.LoginTokenRefresher;

public final class SessionTokenLoginTokenRefresher implements LoginTokenRefresher {

    @Override
    public RefreshTokenLoginCommandOutput refresh(RefreshTokenLoginCommand command) {
        throw new UnsupportedOperationException("Refresh Token is not supported in Session Token mode.");
    }

}
