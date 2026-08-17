package com.cloud.userauth.infrastructure.security;

import com.cloud.userauth.application.port.ChallengeSecretHasher;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.ChallengeSecretHash;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.util.Assert;

public class HmacSha256ChallengeSecretHasher implements ChallengeSecretHasher {
    private final String pepper;

    public HmacSha256ChallengeSecretHasher(String pepper) {
        Assert.hasText(pepper, "pepper must not be blank");
        this.pepper = pepper;
    }

    @Override
    public ChallengeSecretHash hash(AuthChallengeId challengeId, String secret) {
        String message = challengeId.value() + ":" + secret;
        String hash = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, pepper).hmacHex(message);
        return new ChallengeSecretHash(hash);
    }
}
