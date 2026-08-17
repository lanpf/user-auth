package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserRoleGrantDO {
    @Id
    private Long id;
    private Long userId;
    private String roleCode;
    private String sourceType;
    private String sourceId;
    private String status;
    private Long grantedBy;
    private Instant grantedAt;
    private Instant expiresAt;
    private Instant updatedAt;
}
