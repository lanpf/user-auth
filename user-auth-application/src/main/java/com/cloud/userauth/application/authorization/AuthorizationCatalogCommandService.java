package com.cloud.userauth.application.authorization;

import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.PermissionRepository;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.RoleRepository;
import com.cloud.userauth.domain.authorization.service.AuthorizationDomainService;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AuthorizationCatalogCommandService {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AuthorizationDomainService authorizationDomainService;
    private final Clock clock;

    @Transactional
    public PermissionView savePermission(SavePermissionCommand command) {
        PermissionCode code = new PermissionCode(command.permissionCode());
        Instant now = clock.instant();
        Permission permission = permissionRepository.findById(code).orElse(null);
        if (permission == null) {
            permission = Permission.register(
                    code, command.permissionName(), command.ownerService(), now);
        } else {
            permission.updateProfile(command.permissionName(), command.ownerService(), now);
        }
        permissionRepository.save(permission);
        return view(permission);
    }

    @Transactional
    public PermissionView activatePermission(String rawCode) {
        Permission permission = requiredPermission(rawCode);
        permission.activate(clock.instant());
        permissionRepository.save(permission);
        return view(permission);
    }

    @Transactional
    public PermissionView disablePermission(String rawCode) {
        Permission permission = requiredPermission(rawCode);
        permission.disable(clock.instant());
        permissionRepository.save(permission);
        return view(permission);
    }

    @Transactional
    public RoleView saveRole(SaveRoleCommand command) {
        RoleCode code = new RoleCode(command.roleCode());
        List<PermissionCode> permissionCodes = command.permissionCodes().stream()
                .map(PermissionCode::new)
                .distinct()
                .toList();
        authorizationDomainService.validateRolePermissions(permissionCodes);
        Instant now = clock.instant();
        Role role = roleRepository.findById(code).orElse(null);
        if (role == null) {
            role = Role.create(code, command.roleName(), permissionCodes, now);
        } else {
            role.rename(command.roleName(), now);
            role.replacePermissions(permissionCodes, now);
        }
        roleRepository.save(role);
        return view(role);
    }

    @Transactional
    public RoleView activateRole(String rawCode) {
        Role role = requiredRole(rawCode);
        authorizationDomainService.validateRolePermissions(role.permissionCodes());
        role.activate(clock.instant());
        roleRepository.save(role);
        return view(role);
    }

    @Transactional
    public RoleView disableRole(String rawCode) {
        Role role = requiredRole(rawCode);
        role.disable(clock.instant());
        roleRepository.save(role);
        return view(role);
    }

    private Permission requiredPermission(String rawCode) {
        return permissionRepository.findById(new PermissionCode(rawCode))
                .orElseThrow(() -> new DomainException(DomainError.PERMISSION_NOT_FOUND));
    }

    private Role requiredRole(String rawCode) {
        return roleRepository.findById(new RoleCode(rawCode))
                .orElseThrow(() -> new DomainException(DomainError.ROLE_NOT_FOUND));
    }

    static PermissionView view(Permission permission) {
        return new PermissionView(
                permission.getPermissionCode().value(),
                permission.getPermissionName(),
                permission.getOwnerService(),
                permission.getStatus().name(),
                permission.getCreatedAt(),
                permission.getUpdatedAt());
    }

    static RoleView view(Role role) {
        return new RoleView(
                role.getRoleCode().value(),
                role.getRoleName(),
                role.status().name(),
                role.permissionCodes().stream().map(PermissionCode::value).toList(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }
}
