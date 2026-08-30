package com.cloud.userauth.domain.authorization;

import com.cloud.framework.core.validation.Require;
import com.cloud.framework.domain.AggregateRoot;
import java.time.Instant;

import com.cloud.userauth.domain.common.DomainException;
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
        this.permissionName = Require.notBlank(permissionName, DomainException::missingField).trim();
        this.ownerService = Require.notBlank(ownerService, DomainException::missingField).trim();
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
        permissionName = Require.notBlank(name, DomainException::missingField).trim();
        ownerService = Require.notBlank(service, DomainException::missingField).trim();
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
}
