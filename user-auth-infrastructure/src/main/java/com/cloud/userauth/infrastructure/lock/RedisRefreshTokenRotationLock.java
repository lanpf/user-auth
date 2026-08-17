package com.cloud.userauth.infrastructure.lock;

import com.cloud.framework.lock.LockContext;
import com.cloud.framework.lock.LockExecutor;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.RefreshTokenRotationLock;
import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RedisRefreshTokenRotationLock implements RefreshTokenRotationLock {
    private static final String SCENE = "refresh-token-rotation";
    private static final Duration WAIT_TIME = Duration.ofSeconds(3);

    private final LockExecutor lockExecutor;

    @Override
    public <T> T execute(String refreshToken, Supplier<T> action) {
        LockContext context = new LockContext(SCENE, sha256(refreshToken), WAIT_TIME);
        try {
            return lockExecutor.execute(context, action::get)
                    .orElseThrow(() -> new ApplicationException(
                            ApplicationError.APP_REFRESH_TOKEN_FAILED));
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new InfrastructureException(
                    InfrastructureError.INFRA_TECH_LOCK_EXECUTION_FAILED,
                    exception);
        }
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
