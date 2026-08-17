package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import java.util.function.Supplier;

public interface MobileOtpLoginLock {
    <T> T execute(LoginMobile mobile, Supplier<T> action);

    static MobileOtpLoginLock direct() {
        return new MobileOtpLoginLock() {
            @Override
            public <T> T execute(LoginMobile mobile, Supplier<T> action) {
                return action.get();
            }
        };
    }
}
