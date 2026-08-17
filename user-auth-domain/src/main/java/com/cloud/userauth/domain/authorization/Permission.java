package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.AggregateRoot;
import java.time.Instant;
import lombok.Getter;

@Getter
public class Permission implements AggregateRoot<PermissionCode> {
    private final PermissionCode permissionCode;
    private String permissionName;
    private String ownerService;
    private PermissionStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Permission(PermissionCode permissionCode, String permissionName, String ownerService,
                       PermissionStatus status, Instant createdAt, Instant updatedAt) {
        this.permissionCode = permissionCode;
        this.permissionName = requireText(permissionName);
        this.ownerService = requireText(ownerService);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Permission register(PermissionCode code, String name, String ownerService, Instant registeredAt) {
        return new Permission(code, name, ownerService, PermissionStatus.ACTIVE, registeredAt, registeredAt);
    }

    public static Permission restore(PermissionCode code, String name, String ownerService,
                                     PermissionStatus status, Instant createdAt, Instant updatedAt) {
        return new Permission(code, name, ownerService, status, createdAt, updatedAt);
    }

    @Override
    public PermissionCode getId() { return permissionCode; }

    public boolean isActive() { return status == PermissionStatus.ACTIVE; }

    public void updateProfile(String name, String service, Instant changedAt) {
        permissionName = requireText(name);
        ownerService = requireText(service);
        updatedAt = changedAt;
    }

    public void disable(Instant disabledAt) {
        status = PermissionStatus.DISABLED;
        updatedAt = disabledAt;
    }

    public void activate(Instant activatedAt) {
        status = PermissionStatus.ACTIVE;
        updatedAt = activatedAt;
    }

    private static String requireText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new com.cloud.userauth.domain.common.DomainException(
                    com.cloud.userauth.domain.common.DomainError.DOMAIN_FIELD_REQUIRED);
        }
        return value.trim();
    }
}
