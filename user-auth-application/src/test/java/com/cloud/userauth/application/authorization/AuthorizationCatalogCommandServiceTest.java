package com.cloud.userauth.application.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloud.framework.domain.DomainEventId;
import com.cloud.framework.domain.DomainEventIdGenerator;
import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.PermissionRepository;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.RoleRepository;
import com.cloud.userauth.domain.authorization.UserPermissionGrantRepository;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.authorization.service.AuthorizationDomainService;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthorizationCatalogCommandServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-12T00:00:00Z");

    @Test
    void shouldManagePermissionAndRoleCatalog() {
        Permissions permissions = new Permissions();
        Roles roles = new Roles();
        AuthorizationCatalogCommandService service = service(permissions, roles);

        PermissionView permission = service.savePermission(
                new SavePermissionCommand("order:refund", "Refund order", "order-service"));
        RoleView role = service.saveRole(
                new SaveRoleCommand("merchant-admin", "Merchant admin", List.of("order:refund")));
        PermissionView disabledPermission = service.disablePermission("order:refund");
        RoleView disabledRole = service.disableRole("merchant-admin");

        assertEquals("ACTIVE", permission.status());
        assertEquals(List.of("order:refund"), role.permissionCodes());
        assertEquals("DISABLED", disabledPermission.status());
        assertEquals("DISABLED", disabledRole.status());
    }

    @Test
    void shouldRejectRoleReferencingMissingPermission() {
        Permissions permissions = new Permissions();
        Roles roles = new Roles();

        assertThrows(DomainException.class, () -> service(permissions, roles).saveRole(
                new SaveRoleCommand("merchant-admin", "Merchant admin", List.of("order:refund"))));
    }

    private static AuthorizationCatalogCommandService service(
            Permissions permissions,
            Roles roles
    ) {
        AuthorizationDomainService domainService = new AuthorizationDomainService(
                roles,
                emptyRoleGrants(),
                permissions,
                emptyPermissionGrants(),
                new DomainEventIdGenerator() {
                    @Override public DomainEventId nextId() { return new DomainEventId(1L); }
                });
        return new AuthorizationCatalogCommandService(
                permissions, roles, domainService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static UserRoleGrantRepository emptyRoleGrants() {
        return new UserRoleGrantRepository() {
            @Override public com.cloud.userauth.domain.authorization.GrantId nextId() { throw new UnsupportedOperationException(); }
            @Override public void save(com.cloud.userauth.domain.authorization.UserRoleGrant value) { throw new UnsupportedOperationException(); }
            @Override public Optional<com.cloud.userauth.domain.authorization.UserRoleGrant> findById(com.cloud.userauth.domain.authorization.GrantId id) { return Optional.empty(); }
            @Override public List<com.cloud.userauth.domain.authorization.UserRoleGrant> findActiveByUserId(com.cloud.userauth.domain.user.UserId userId) { return List.of(); }
            @Override public Optional<com.cloud.userauth.domain.authorization.UserRoleGrant> findActiveByUserIdAndRoleCodeAndSource(com.cloud.userauth.domain.user.UserId userId, RoleCode roleCode, com.cloud.userauth.domain.authorization.GrantSource source) { return Optional.empty(); }
            @Override public List<com.cloud.userauth.domain.authorization.UserRoleGrant> findActiveByUserIdAndSource(com.cloud.userauth.domain.user.UserId userId, com.cloud.userauth.domain.authorization.GrantSource source) { return List.of(); }
            @Override public boolean existsActiveByUserIdAndRoleCodeAndSource(com.cloud.userauth.domain.user.UserId userId, RoleCode roleCode, com.cloud.userauth.domain.authorization.GrantSource source) { return false; }
        };
    }

    private static UserPermissionGrantRepository emptyPermissionGrants() {
        return new UserPermissionGrantRepository() {
            @Override public com.cloud.userauth.domain.authorization.GrantId nextId() { throw new UnsupportedOperationException(); }
            @Override public void save(com.cloud.userauth.domain.authorization.UserPermissionGrant value) { throw new UnsupportedOperationException(); }
            @Override public Optional<com.cloud.userauth.domain.authorization.UserPermissionGrant> findById(com.cloud.userauth.domain.authorization.GrantId id) { return Optional.empty(); }
            @Override public List<com.cloud.userauth.domain.authorization.UserPermissionGrant> findActiveByUserId(com.cloud.userauth.domain.user.UserId userId) { return List.of(); }
            @Override public Optional<com.cloud.userauth.domain.authorization.UserPermissionGrant> findActiveByUserIdAndPermissionCodeAndSource(com.cloud.userauth.domain.user.UserId userId, PermissionCode permissionCode, com.cloud.userauth.domain.authorization.GrantSource source) { return Optional.empty(); }
            @Override public List<com.cloud.userauth.domain.authorization.UserPermissionGrant> findActiveByUserIdAndSource(com.cloud.userauth.domain.user.UserId userId, com.cloud.userauth.domain.authorization.GrantSource source) { return List.of(); }
            @Override public boolean existsActiveByUserIdAndPermissionCodeAndSource(com.cloud.userauth.domain.user.UserId userId, PermissionCode permissionCode, com.cloud.userauth.domain.authorization.GrantSource source) { return false; }
        };
    }

    private static final class Permissions implements PermissionRepository {
        private final Map<PermissionCode, Permission> values = new LinkedHashMap<>();
        @Override public PermissionCode nextId() { throw new UnsupportedOperationException(); }
        @Override public void save(Permission value) { values.put(value.getId(), value); }
        @Override public Optional<Permission> findById(PermissionCode id) { return Optional.ofNullable(values.get(id)); }
        @Override public List<Permission> findByIds(Collection<PermissionCode> ids) { return ids.stream().map(values::get).filter(java.util.Objects::nonNull).toList(); }
        @Override public com.cloud.framework.domain.PagedList<Permission> findPermissions(int pageNo, int pageSize) {
            return new com.cloud.framework.domain.PagedList<>(List.copyOf(values.values()), (long) values.size());
        }
    }

    private static final class Roles implements RoleRepository {
        private final Map<RoleCode, Role> values = new LinkedHashMap<>();
        @Override public RoleCode nextId() { throw new UnsupportedOperationException(); }
        @Override public void save(Role value) { values.put(value.getId(), value); }
        @Override public Optional<Role> findById(RoleCode id) { return Optional.ofNullable(values.get(id)); }
        @Override public Optional<Role> findByRoleCode(RoleCode roleCode) { return findById(roleCode); }
        @Override public boolean existsByRoleCode(RoleCode roleCode) { return values.containsKey(roleCode); }
        @Override public com.cloud.framework.domain.PagedList<Role> findRoles(int pageNo, int pageSize) {
            return new com.cloud.framework.domain.PagedList<>(List.copyOf(values.values()), (long) values.size());
        }
    }
}
