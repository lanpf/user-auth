package com.cloud.userauth.application.port;

import com.cloud.userauth.domain.authentication.challenge.AuthChallengeType;
import com.cloud.userauth.domain.authentication.challenge.ChallengeTarget;

public interface AuthChallengeDispatcher {
    void dispatch(AuthChallengeType type, ChallengeTarget target, String secret);
}
