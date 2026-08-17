package com.cloud.userauth.infrastructure.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.framework.lock.LockContext;
import com.cloud.framework.lock.LockExecutor;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import com.cloud.userauth.domain.authentication.credential.LoginMobile;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

class RedisLockKeyTest {
    @Test
    void shouldUseReadableMobileOtpLoginLockKey() {
        CapturingLockExecutor executor = new CapturingLockExecutor();
        RedisMobileOtpLoginLock lock = new RedisMobileOtpLoginLock(executor);

        String result = lock.execute(new LoginMobile("13800138000"), () -> "completed");

        assertEquals("completed", result);
        assertEquals("mobile-login", executor.context.scene());
        assertEquals("13800138000", executor.context.key());
        assertEquals("mobile-login:13800138000", executor.context.getLockName());
    }

    @Test
    void shouldUseReadableChallengeIssueLockKey() {
        CapturingLockExecutor executor = new CapturingLockExecutor();
        RedisAuthChallengeIssueLock lock = new RedisAuthChallengeIssueLock(executor);

        String result = lock.execute(
                AuthChallengeType.SMS_OTP,
                new ChallengeTarget("13800138000"),
                AuthChallengeScene.LOGIN,
                () -> "completed");

        assertEquals("completed", result);
        assertEquals("challenge-issue", executor.context.scene());
        assertEquals("SMS_OTP:LOGIN:13800138000", executor.context.key());
        assertEquals(
                "challenge-issue:SMS_OTP:LOGIN:13800138000",
                executor.context.getLockName());
    }

    @Test
    void shouldHashRefreshTokenInRotationLockKey() {
        CapturingLockExecutor executor = new CapturingLockExecutor();
        RedisRefreshTokenRotationLock lock = new RedisRefreshTokenRotationLock(executor);

        String result = lock.execute("sensitive-refresh-token", () -> "completed");

        assertEquals("completed", result);
        assertEquals("refresh-token-rotation", executor.context.scene());
        assertEquals(
                "3b7bef4289af3728d9c1c6d5cd3362b289a5c7cd258cad35194b3eb188ae7d53",
                executor.context.key());
    }

    private static final class CapturingLockExecutor implements LockExecutor {
        private LockContext context;

        @Override
        public <T> Optional<T> execute(LockContext context, Callable<T> callable) throws Exception {
            this.context = context;
            return Optional.ofNullable(callable.call());
        }
    }
}
