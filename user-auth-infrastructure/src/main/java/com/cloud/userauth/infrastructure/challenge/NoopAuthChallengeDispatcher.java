package com.cloud.userauth.infrastructure.challenge;

import com.cloud.userauth.application.port.AuthChallengeDispatcher;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopAuthChallengeDispatcher implements AuthChallengeDispatcher {
    @Override
    public void dispatch(AuthChallengeType type, ChallengeTarget target, String secret) {
        log.info("Challenge dispatch requested: type={}, target={}, secret={}",
                type, target.value(), secret);
    }
}
