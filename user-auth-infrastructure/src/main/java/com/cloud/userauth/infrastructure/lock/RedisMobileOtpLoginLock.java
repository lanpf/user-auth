package com.cloud.userauth.infrastructure.lock;

import com.cloud.framework.lock.LockContext;
import com.cloud.framework.lock.LockExecutor;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.MobileOtpLoginLock;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import java.time.Duration;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisMobileOtpLoginLock implements MobileOtpLoginLock {
    private static final String SCENE = "mobile-login";
    private static final Duration WAIT_TIME = Duration.ofSeconds(3);

    private final LockExecutor lockExecutor;

    @Override
    public <T> T execute(LoginMobile mobile, Supplier<T> action) {
        LockContext context = new LockContext(SCENE, mobile.value(), WAIT_TIME);
        try {
            return lockExecutor.execute(context, action::get)
                    .orElseThrow(() -> new ApplicationException(ApplicationError.APP_MOBILE_LOGIN_IN_PROGRESS));
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new InfrastructureException(InfrastructureError.INFRA_TECH_LOCK_EXECUTION_FAILED, exception);
        }
    }

}
