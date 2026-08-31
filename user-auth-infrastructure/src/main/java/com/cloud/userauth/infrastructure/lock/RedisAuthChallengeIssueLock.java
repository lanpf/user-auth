package com.cloud.userauth.infrastructure.lock;

import com.cloud.framework.lock.LockContext;
import com.cloud.framework.lock.LockExecutor;
import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.application.port.AuthChallengeIssueLock;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import java.time.Duration;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisAuthChallengeIssueLock implements AuthChallengeIssueLock {
    private static final Duration WAIT_TIME = Duration.ofSeconds(3);
    private final LockExecutor lockExecutor;

    @Override
    public <T> T execute(AuthChallengeType type, AuthChallengeScene scene,
                         ChallengeTarget target, Supplier<T> action) {
        LockContext context = new LockContext(WAIT_TIME, "challenge-issue", type.name(), scene.name(), target.value());
        try {
            return lockExecutor.execute(context, action::get).orElseThrow(
                    () -> new ApplicationException(ApplicationError.APP_AUTH_CHALLENGE_ISSUE_IN_PROGRESS));
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
