package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class LoginAttemptDO {
    @Id
    private String id;
    private String issuer;
    private String issuerType;
    private String principal;
    private String mobile;
    private boolean mobileVerified;
    private String status;
    private String sessionId;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
