package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class Role implements AggregateRoot<RoleCode> {
    private final RoleCode roleCode;
    private String roleName;
    private RoleStatus status;
    private final List<PermissionCode> permissionCodes;
    private final Instant createdAt;
    private Instant updatedAt;

    private Role(
            RoleCode roleCode,
            String roleName,
            RoleStatus status,
            List<PermissionCode> permissionCodes,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.roleCode = roleCode;
        this.roleName = requireText(roleName);
        this.status = status;
        this.permissionCodes = new ArrayList<>(permissionCodes.stream().distinct().toList());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Role create(
            RoleCode roleCode,
            String roleName,
            List<PermissionCode> permissionCodes,
            Instant createdAt
    ) {
        return new Role(
                roleCode, roleName, RoleStatus.ACTIVE,
                permissionCodes, createdAt, createdAt);
    }

    public static Role restore(RoleCode roleCode, String roleName, RoleStatus status,
                               List<PermissionCode> permissionCodes, Instant createdAt, Instant updatedAt) {
        return new Role(roleCode, roleName, status, permissionCodes, createdAt, updatedAt);
    }

    public List<PermissionCode> permissionCodes() {
        return Collections.unmodifiableList(permissionCodes);
    }

    @Override
    public RoleCode getId() {
        return roleCode;
    }

    public RoleStatus status() {
        return status;
    }

    public boolean hasPermission(PermissionCode permissionCode) {
        if (status != RoleStatus.ACTIVE) {
            return false;
        }
        return permissionCodes.contains(permissionCode);
    }

    public void rename(String newRoleName, Instant renamedAt) {
        this.roleName = requireText(newRoleName);
        this.updatedAt = renamedAt;
    }

    public void replacePermissions(List<PermissionCode> newPermissionCodes, Instant changedAt) {
        permissionCodes.clear();
        permissionCodes.addAll(newPermissionCodes.stream().distinct().toList());
        updatedAt = changedAt;
    }

    public void disable(Instant disabledAt) {
        this.status = RoleStatus.DISABLED;
        this.updatedAt = disabledAt;
    }

    public void activate(Instant activatedAt) {
        this.status = RoleStatus.ACTIVE;
        this.updatedAt = activatedAt;
    }

    private static String requireText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        return value.trim();
    }
}
