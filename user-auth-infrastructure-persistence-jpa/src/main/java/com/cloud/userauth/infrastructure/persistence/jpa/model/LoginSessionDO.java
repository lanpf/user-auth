package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class LoginSessionDO {
    @Id
    private String id;
    private Long userId;
    private Long authAccountId;
    private Long authenticatedCredentialId;
    private String status;
    private String loginScene;
    private String deviceId;
    private String deviceType;
    private String deviceName;
    private String clientAppId;
    private String clientPlatform;
    private String clientVersion;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant lastActiveAt;
}
