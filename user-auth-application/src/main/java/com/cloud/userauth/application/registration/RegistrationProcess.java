package com.cloud.userauth.application.registration;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.domain.authentication.account.AuthAccountId;
import com.cloud.userauth.domain.authentication.challenge.AuthChallengeId;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class RegistrationProcess {
    private final RegistrationProcessId id;
    private final AuthChallengeId challengeId;
    private final UserId userId;
    private final AuthAccountId authAccountId;
    private RegistrationProcessStatus status;
    private String lastFailure;
    private int retryCount;
    private Instant lastFailedAt;
    private SessionId sessionId;
    private final Instant createdAt;
    private Instant updatedAt;

    private RegistrationProcess(
            RegistrationProcessId id,
            AuthChallengeId challengeId,
            UserId userId,
            AuthAccountId authAccountId,
            RegistrationProcessStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.challengeId = challengeId;
        this.userId = userId;
        this.authAccountId = authAccountId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RegistrationProcess accountCreated(
            RegistrationProcessId id,
            AuthChallengeId challengeId,
            UserId userId,
            AuthAccountId authAccountId,
            Instant createdAt
    ) {
        return new RegistrationProcess(
                id, challengeId, userId, authAccountId,
                RegistrationProcessStatus.AUTH_ACCOUNT_CREATED, createdAt, createdAt
        );
    }

    public static RegistrationProcess restore(
            RegistrationProcessId id,
            AuthChallengeId challengeId,
            UserId userId,
            AuthAccountId authAccountId,
            RegistrationProcessStatus status,
            String lastFailure,
            int retryCount,
            Instant lastFailedAt,
            SessionId sessionId,
            Instant createdAt,
            Instant updatedAt
    ) {
        RegistrationProcess process = new RegistrationProcess(
                id, challengeId, userId, authAccountId, status, createdAt, updatedAt);
        process.lastFailure = lastFailure;
        process.retryCount = retryCount;
        process.lastFailedAt = lastFailedAt;
        process.sessionId = sessionId;
        return process;
    }

    public void markUserInitialized(Instant initializedAt) {
        ensureNotTerminated();
        status = RegistrationProcessStatus.USER_INITIALIZED;
        lastFailure = null;
        lastFailedAt = null;
        updatedAt = initializedAt;
    }

    public void markRetryableFailure(String failure, Instant failedAt) {
        ensureNotTerminated();
        lastFailure = failure;
        retryCount++;
        lastFailedAt = failedAt;
        updatedAt = failedAt;
    }

    public void complete(SessionId sessionId, Instant completedAt) {
        ensureNotTerminated();
        if (status != RegistrationProcessStatus.USER_INITIALIZED
                && status != RegistrationProcessStatus.COMPLETED) {
            throw new ApplicationException(ApplicationError.APP_REGISTRATION_PROCESS_NOT_READY);
        }
        if (status == RegistrationProcessStatus.COMPLETED) {
            return;
        }
        this.sessionId = sessionId;
        this.status = RegistrationProcessStatus.COMPLETED;
        this.lastFailure = null;
        this.lastFailedAt = null;
        this.updatedAt = completedAt;
    }

    public boolean needsUserInitialization() {
        return status == RegistrationProcessStatus.AUTH_ACCOUNT_CREATED;
    }

    public boolean isCompleted() {
        return status == RegistrationProcessStatus.COMPLETED;
    }

    private void ensureNotTerminated() {
        if (status == RegistrationProcessStatus.TERMINATED) {
            throw new ApplicationException(ApplicationError.APP_REGISTRATION_PROCESS_TERMINATED);
        }
    }

    public RegistrationProcessId id() {
        return id;
    }
}
