package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RegistrationProcessDO {
    @Id
    private Long id;
    private Long challengeId;
    private Long userId;
    private Long authAccountId;
    private String status;
    private String lastFailure;
    private int retryCount;
    private Instant lastFailedAt;
    private String sessionId;
    private Instant createdAt;
    private Instant updatedAt;
}
