package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RoleDO {
    @Id
    private String roleCode;
    private String roleName;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
