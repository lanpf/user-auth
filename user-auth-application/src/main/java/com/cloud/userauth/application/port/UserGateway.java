package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.user.UserId;

public interface UserGateway {
    void initializeUser(UserId userId);
}
