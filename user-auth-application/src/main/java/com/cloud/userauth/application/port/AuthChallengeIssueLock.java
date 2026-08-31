package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.challenge.AuthChallengeScene;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import java.util.function.Supplier;

public interface AuthChallengeIssueLock {
    <T> T execute(AuthChallengeType type, AuthChallengeScene scene,
                  ChallengeTarget target, Supplier<T> action);

    static AuthChallengeIssueLock direct() {
        return new AuthChallengeIssueLock() {
            @Override
            public <T> T execute(AuthChallengeType type, AuthChallengeScene scene,
                                 ChallengeTarget target, Supplier<T> action) {
                return action.get();
            }
        };
    }
}
