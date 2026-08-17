package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CredentialDO {
    @Id
    private Long id;
    private Long authAccountId;
    private String credentialType;
    private String issuer;
    private String issuerType;
    private String principal;
    private String status;
    private Instant verifiedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
