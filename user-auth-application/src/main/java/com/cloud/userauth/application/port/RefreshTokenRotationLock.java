package com.cloud.userauth.application.port;

import java.util.function.Supplier;

public interface RefreshTokenRotationLock {
    <T> T execute(String refreshToken, Supplier<T> action);

    static RefreshTokenRotationLock direct() {
        return new RefreshTokenRotationLock() {
            @Override
            public <T> T execute(String refreshToken, Supplier<T> action) {
                return action.get();
            }
        };
    }
}
