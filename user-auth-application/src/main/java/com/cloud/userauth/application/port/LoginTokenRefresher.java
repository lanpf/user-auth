package com.cloud.userauth.application.port;

import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommandOutput;

public interface LoginTokenRefresher {
    RefreshTokenLoginCommandOutput refresh(RefreshTokenLoginCommand command);
}
