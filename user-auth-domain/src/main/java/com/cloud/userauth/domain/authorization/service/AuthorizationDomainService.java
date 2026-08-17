package com.cloud.userauth.domain.authorization.service;

import com.cloud.framework.domain.DomainEventIdGenerator;
import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionRepository;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.RoleRepository;
import com.cloud.userauth.domain.authorization.RoleStatus;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserPermissionGrantRepository;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.authorization.event.RoleGrantedEvent;
import com.cloud.userauth.domain.authorization.event.RoleRevokedEvent;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthorizationDomainService {
    private final RoleRepository roleRepository;
    private final UserRoleGrantRepository grantRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionGrantRepository userPermissionGrantRepository;
    private final DomainEventIdGenerator domainEventIdGenerator;

    public RoleGrantEffect grantRole(
            GrantId grantId,
            UserId userId,
            RoleCode roleCode,
            GrantSource source,
            UserId grantedBy,
            Instant grantedAt,
            Instant expiresAt
    ) {
        Role role = roleRepository.findById(roleCode)
                .orElseThrow(() -> new DomainException(DomainError.ROLE_NOT_FOUND));
        if (role.status() != RoleStatus.ACTIVE) {
            throw new DomainException(DomainError.ROLE_DISABLED);
        }
        assertAllPermissionsActive(role.permissionCodes());
        UserRoleGrant existing = grantRepository.findActiveByUserIdAndRoleCodeAndSource(userId, roleCode, source)
                .orElse(null);
        if (existing != null) {
            return new RoleGrantEffect(existing, List.of());
        }
        UserRoleGrant grant = UserRoleGrant.grant(
                grantId, userId, roleCode, source, grantedBy, grantedAt, expiresAt);
        return new RoleGrantEffect(
                grant,
                List.of(new RoleGrantedEvent(
                        domainEventIdGenerator.nextId(), grantedAt, grant.id(), grant.userId(), grant.roleCode()
                ))
        );
    }

    public RoleGrantEffect revokeRole(UserId userId, RoleCode roleCode, GrantSource source, Instant revokedAt) {
        UserRoleGrant grant = grantRepository.findActiveByUserIdAndRoleCodeAndSource(userId, roleCode, source)
                .orElseThrow(() -> new DomainException(DomainError.USER_ROLE_GRANT_NOT_FOUND));
        grant.revoke(revokedAt);
        return new RoleGrantEffect(
                grant,
                List.of(new RoleRevokedEvent(
                        domainEventIdGenerator.nextId(), revokedAt, grant.id(), grant.userId(), grant.roleCode()
                ))
        );
    }

    public boolean hasPermission(
            UserId userId,
            PermissionCode permissionCode,
            Instant checkedAt
    ) {
        if (!isPermissionActive(permissionCode)) {
            return false;
        }
        boolean grantedByRole = grantRepository.findActiveByUserId(userId).stream()
                .filter(grant -> grant.isActiveAt(checkedAt))
                .map(grant -> roleRepository.findById(grant.roleCode()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .anyMatch(role -> role.hasPermission(permissionCode));
        return grantedByRole || userPermissionGrantRepository.findActiveByUserId(userId).stream()
                .anyMatch(grant -> grant.getPermissionCode().equals(permissionCode) && grant.isActiveAt(checkedAt));
    }

    public void checkPermission(
            UserId userId,
            PermissionCode permissionCode,
            Instant checkedAt
    ) {
        if (!hasPermission(userId, permissionCode, checkedAt)) {
            throw new DomainException(DomainError.ROLE_PERMISSION_DENIED);
        }
    }

    public Permission registerPermission(PermissionCode permissionCode, String permissionName,
                                         String ownerService, Instant registeredAt) {
        if (permissionRepository.existsById(permissionCode)) {
            throw new DomainException(DomainError.PERMISSION_ALREADY_EXISTS);
        }
        return Permission.register(permissionCode, permissionName, ownerService, registeredAt);
    }

    public void disablePermission(PermissionCode permissionCode, Instant disabledAt) {
        Permission permission = permissionRepository.findById(permissionCode)
                .orElseThrow(() -> new DomainException(DomainError.PERMISSION_NOT_FOUND));
        permission.disable(disabledAt);
    }

    public UserPermissionGrant grantPermission(UserId userId, PermissionCode permissionCode, GrantSource source,
                                               UserId grantedBy, Instant grantedAt, Instant expiresAt, String reason) {
        assertPermissionActive(permissionCode);
        UserPermissionGrant existing = userPermissionGrantRepository
                .findActiveByUserIdAndPermissionCodeAndSource(userId, permissionCode, source)
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        return UserPermissionGrant.grant(userPermissionGrantRepository.nextId(), userId,
                permissionCode, source, grantedBy, grantedAt, expiresAt, reason);
    }

    public void revokePermission(UserId userId, PermissionCode permissionCode, GrantSource source, Instant revokedAt) {
        UserPermissionGrant grant = userPermissionGrantRepository
                .findActiveByUserIdAndPermissionCodeAndSource(userId, permissionCode, source)
                .orElseThrow(() -> new DomainException(DomainError.USER_PERMISSION_GRANT_NOT_FOUND));
        grant.revoke(revokedAt);
    }

    private boolean isPermissionActive(PermissionCode permissionCode) {
        return permissionRepository.findById(permissionCode)
                .map(Permission::isActive).orElse(false);
    }

    private void assertPermissionActive(PermissionCode permissionCode) {
        Permission permission = permissionRepository.findById(permissionCode)
                .orElseThrow(() -> new DomainException(DomainError.PERMISSION_NOT_FOUND));
        if (!permission.isActive()) {
            throw new DomainException(DomainError.PERMISSION_DISABLED);
        }
    }

    private void assertAllPermissionsActive(List<PermissionCode> permissionCodes) {
        List<Permission> permissions = permissionRepository.findByIds(permissionCodes);
        if (permissions.size() != permissionCodes.size()) {
            throw new DomainException(DomainError.PERMISSION_NOT_FOUND);
        }
        permissions.forEach(permission -> {
            if (!permission.isActive()) {
                throw new DomainException(DomainError.PERMISSION_DISABLED);
            }
        });
    }

    public void validatePolicyTargets(
            List<RoleCode> roleCodes,
            List<PermissionCode> directPermissionCodes
    ) {
        for (RoleCode roleCode : roleCodes) {
            Role role = roleRepository.findById(roleCode)
                    .orElseThrow(() -> new DomainException(DomainError.ROLE_NOT_FOUND));
            if (role.status() != RoleStatus.ACTIVE) {
                throw new DomainException(DomainError.ROLE_DISABLED);
            }
            assertAllPermissionsActive(role.permissionCodes());
        }
        assertAllPermissionsActive(directPermissionCodes);
    }

    public void validateRolePermissions(List<PermissionCode> permissionCodes) {
        assertAllPermissionsActive(permissionCodes);
    }
}
