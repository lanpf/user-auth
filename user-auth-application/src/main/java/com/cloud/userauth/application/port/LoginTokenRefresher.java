package com.cloud.userauth.application.port;

import com.cloud.userauth.application.login.refresh.RefreshTokenLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshTokenLoginOutput;

public interface LoginTokenRefresher {
    RefreshTokenLoginOutput refresh(RefreshTokenLoginCommand command);
}
