package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PermissionDO {
    @Id
    private String permissionCode;
    private String permissionName;
    private String ownerService;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
