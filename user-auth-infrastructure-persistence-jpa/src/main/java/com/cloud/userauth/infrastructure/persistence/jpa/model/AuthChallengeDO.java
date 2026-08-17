package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AuthChallengeDO {
    @Id
    private Long id;
    private String challengeType;
    private String target;
    private String scene;
    private String secretHash;
    private String status;
    private Instant expiresAt;
    private Instant reusableUntil;
    private int attempts;
    private Instant verifiedAt;
    private String consumedByType;
    private String consumedById;
    private Instant createdAt;
    private Instant updatedAt;
}
