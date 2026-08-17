package com.cloud.userauth.application.port;

import com.cloud.userauth.application.login.refresh.RefreshLoginCommand;
import com.cloud.userauth.application.login.refresh.RefreshLoginCommandOutput;

public interface LoginTokenRefresher {
    RefreshLoginCommandOutput refresh(RefreshLoginCommand command);
}
