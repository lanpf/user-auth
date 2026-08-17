package com.cloud.userauth.domain.authentication.challenge;

import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Instant;
import lombok.Getter;

@Getter
public class AuthChallenge implements AggregateRoot<AuthChallengeId> {
    public static final int DEFAULT_MAX_ATTEMPTS = 5;

    private final AuthChallengeId id;
    private final AuthChallengeType type;
    private final ChallengeTarget target;
    private final AuthChallengeScene scene;
    private final ChallengeSecretHash secretHash;
    private AuthChallengeStatus status;
    private final Instant expiresAt;
    private final Instant reusableUntil;
    private int attempts;
    private Instant verifiedAt;
    private ChallengeConsumerType consumedByType;
    private String consumedById;
    private final Instant createdAt;
    private Instant updatedAt;

    private AuthChallenge(
            AuthChallengeId id,
            AuthChallengeType type,
            ChallengeTarget target,
            AuthChallengeScene scene,
            ChallengeSecretHash secretHash,
            AuthChallengeStatus status,
            Instant expiresAt,
            Instant reusableUntil,
            int attempts,
            Instant verifiedAt,
            ChallengeConsumerType consumedByType,
            String consumedById,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.scene = scene;
        this.secretHash = secretHash;
        this.status = status;
        this.expiresAt = expiresAt;
        this.reusableUntil = reusableUntil;
        this.attempts = attempts;
        this.verifiedAt = verifiedAt;
        this.consumedByType = consumedByType;
        this.consumedById = consumedById;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AuthChallenge issue(
            AuthChallengeId id,
            AuthChallengeType type,
            ChallengeTarget target,
            AuthChallengeScene scene,
            ChallengeSecretHash secretHash,
            Instant issuedAt,
            Instant expiresAt,
            Instant reusableUntil
    ) {
        if (type == null || scene == null || issuedAt == null
                || expiresAt == null || reusableUntil == null
                || !expiresAt.isAfter(issuedAt) || !reusableUntil.isAfter(issuedAt)
                || reusableUntil.isAfter(expiresAt)) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        return new AuthChallenge(
                id, type, target, scene, secretHash, AuthChallengeStatus.ISSUED,
                expiresAt, reusableUntil,
                0, null, null, null, issuedAt, issuedAt
        );
    }

    public static AuthChallenge restore(
            AuthChallengeId id,
            AuthChallengeType type,
            ChallengeTarget target,
            AuthChallengeScene scene,
            ChallengeSecretHash secretHash,
            AuthChallengeStatus status,
            Instant expiresAt,
            Instant reusableUntil,
            int attempts,
            Instant verifiedAt,
            ChallengeConsumerType consumedByType,
            String consumedById,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new AuthChallenge(
                id, type, target, scene, secretHash, status, expiresAt, reusableUntil, attempts,
                verifiedAt, consumedByType, consumedById, createdAt, updatedAt);
    }

    public void verify(
            AuthChallengeType expectedType,
            AuthChallengeScene expectedScene,
            ChallengeSecretHash providedHash,
            Instant verifiedAt
    ) {
        if (type != expectedType) {
            throw new DomainException(DomainError.AUTH_CHALLENGE_TYPE_MISMATCH);
        }
        if (scene != expectedScene) {
            throw new DomainException(DomainError.AUTH_CHALLENGE_SCENE_MISMATCH);
        }
        if (status == AuthChallengeStatus.EXPIRED || !verifiedAt.isBefore(expiresAt)) {
            status = AuthChallengeStatus.EXPIRED;
            updatedAt = verifiedAt;
            throw new DomainException(DomainError.AUTH_CHALLENGE_EXPIRED);
        }
        if (status == AuthChallengeStatus.CONSUMED) {
            if (secretHash.matches(providedHash)) {
                return;
            }
            throw new DomainException(DomainError.AUTH_CHALLENGE_INVALID);
        }
        if (status == AuthChallengeStatus.VERIFIED && secretHash.matches(providedHash)) {
            return;
        }
        if (status == AuthChallengeStatus.FAILED || attempts >= DEFAULT_MAX_ATTEMPTS) {
            status = AuthChallengeStatus.FAILED;
            updatedAt = verifiedAt;
            throw new DomainException(DomainError.AUTH_CHALLENGE_RETRY_EXCEEDED);
        }
        attempts++;
        updatedAt = verifiedAt;
        if (!secretHash.matches(providedHash)) {
            if (attempts >= DEFAULT_MAX_ATTEMPTS) {
                status = AuthChallengeStatus.FAILED;
            }
            throw new DomainException(DomainError.AUTH_CHALLENGE_INVALID);
        }
        status = AuthChallengeStatus.VERIFIED;
        verifiedAt = updatedAt;
    }

    public void consume(
            ChallengeConsumerType consumerType,
            String consumerId,
            Instant consumedAt
    ) {
        if (status == AuthChallengeStatus.CONSUMED) {
            if (consumedByType == consumerType && consumedById.equals(consumerId)) {
                return;
            }
            throw new DomainException(DomainError.AUTH_CHALLENGE_CONSUMED);
        }
        if (status != AuthChallengeStatus.VERIFIED) {
            throw new DomainException(DomainError.AUTH_CHALLENGE_INVALID);
        }
        if (consumerType == null || consumerId == null) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        status = AuthChallengeStatus.CONSUMED;
        consumedByType = consumerType;
        consumedById = consumerId;
        updatedAt = consumedAt;
    }

    public boolean isReusableAt(Instant now) {
        return status == AuthChallengeStatus.ISSUED
                && now.isBefore(reusableUntil)
                && now.isBefore(expiresAt);
    }

    @Override
    public AuthChallengeId id() {
        return id;
    }
}
