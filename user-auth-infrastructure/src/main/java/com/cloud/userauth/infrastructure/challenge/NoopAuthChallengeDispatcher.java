package com.cloud.userauth.infrastructure.challenge;

import com.cloud.userauth.application.port.AuthChallengeDispatcher;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;
import com.cloud.userauth.infrastructure.security.SensitiveValueCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NoopAuthChallengeDispatcher implements AuthChallengeDispatcher {

    private final SensitiveValueCipher cipher;

    @Override
    public void dispatch(AuthChallengeType type, ChallengeTarget target, String secret) {
        if (cipher == null) {
            log.info("Challenge dispatch requested: type={}", type);
            return;
        }
        log.info("Challenge dispatch requested: type={}, target={}, credential={}",
                type, cipher.encrypt(target.value()), cipher.encrypt(secret));
    }
}
