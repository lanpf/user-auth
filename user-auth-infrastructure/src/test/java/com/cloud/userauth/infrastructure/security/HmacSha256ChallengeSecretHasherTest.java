package com.cloud.userauth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.challenge.ChallengeSecretHash;
import org.junit.jupiter.api.Test;

class HmacSha256ChallengeSecretHasherTest {
    private final HmacSha256ChallengeSecretHasher hasher = new HmacSha256ChallengeSecretHasher("test-pepper");

    @Test
    void shouldGenerateStableHmacForSameChallengeAndSecret() {
        AuthChallengeId challengeId = new AuthChallengeId(1001L);

        ChallengeSecretHash first = hasher.hash(challengeId, "123456");
        ChallengeSecretHash second = hasher.hash(challengeId, "123456");

        assertEquals(first, second);
        assertEquals(64, first.value().length());
    }

    @Test
    void shouldBindHashToChallengeId() {
        ChallengeSecretHash first = hasher.hash(new AuthChallengeId(1001L), "123456");
        ChallengeSecretHash second = hasher.hash(new AuthChallengeId(1002L), "123456");

        assertNotEquals(first, second);
    }

    @Test
    void shouldRejectMissingPepper() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new HmacSha256ChallengeSecretHasher(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new HmacSha256ChallengeSecretHasher(""));
        assertThrows(
                IllegalArgumentException.class,
                () -> new HmacSha256ChallengeSecretHasher("   "));
    }
}
