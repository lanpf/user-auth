package com.cloud.userauth.domain.authorization.service;

import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.GrantSourceType;
import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.PermissionRepository;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.RoleRepository;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserPermissionGrantRepository;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationDomainServiceTest {
    @Test
    void shouldAllowAnActiveDirectPermissionGrant() {
        PermissionCode code = new PermissionCode("order:refund");
        InMemoryPermissions permissions = new InMemoryPermissions(Permission.register(code, "退款", "order", Instant.EPOCH));
        InMemoryPermissionGrants grants = new InMemoryPermissionGrants();
        AuthorizationDomainService service = new AuthorizationDomainService(new InMemoryRoles(), new InMemoryRoleGrants(),
                permissions, grants, () -> new DomainEventId(1L));
        UserId userId = new UserId(10L);
        UserPermissionGrant grant = service.grantPermission(
                userId, code, channelSource("mini-program"), new UserId(1L), Instant.EPOCH, null, "temporary");
        grants.save(grant);
        assertTrue(service.hasPermission(userId, code, Instant.EPOCH));
    }

    @Test
    void shouldKeepGrantsFromDifferentSourcesAndDeduplicateSameSource() {
        PermissionCode code = new PermissionCode("order:refund");
        InMemoryPermissions permissions = new InMemoryPermissions(Permission.register(code, "退款", "order", Instant.EPOCH));
        InMemoryPermissionGrants grants = new InMemoryPermissionGrants();
        AuthorizationDomainService service = new AuthorizationDomainService(new InMemoryRoles(), new InMemoryRoleGrants(),
                permissions, grants, () -> new DomainEventId(1L));
        UserId userId = new UserId(10L);

        UserPermissionGrant miniProgram = service.grantPermission(
                userId, code, channelSource("mini-program"), null, Instant.EPOCH, null, "initial policy");
        grants.save(miniProgram);
        UserPermissionGrant repeated = service.grantPermission(
                userId, code, channelSource("mini-program"), null, Instant.EPOCH, null, "initial policy");
        UserPermissionGrant merchantPortal = service.grantPermission(
                userId, code, channelSource("merchant-portal"), null, Instant.EPOCH, null, "initial policy");
        grants.save(merchantPortal);

        org.junit.jupiter.api.Assertions.assertSame(miniProgram, repeated);
        org.junit.jupiter.api.Assertions.assertEquals(2, grants.findActiveByUserId(userId).size());
    }

    private static GrantSource channelSource(String channelCode) {
        return new GrantSource(GrantSourceType.CHANNEL_AUTHORIZATION_POLICY, channelCode);
    }

    private static final class InMemoryPermissions implements PermissionRepository {
        private final Map<PermissionCode, Permission> values = new HashMap<>();
        InMemoryPermissions(Permission permission) { save(permission); }
        public PermissionCode nextId() { return new PermissionCode("unused:unused"); }
        public void save(Permission value) { values.put(value.id(), value); }
        public Optional<Permission> findById(PermissionCode id) { return Optional.ofNullable(values.get(id));}
        public List<Permission> findByIds(Collection<PermissionCode> ids) { return ids.stream().map(values::get).filter(Objects::nonNull).toList(); }
        public com.cloud.framework.domain.PagedList<Permission> findPermissions(int pageNo, int pageSize) {
            return new com.cloud.framework.domain.PagedList<>(List.copyOf(values.values()), (long) values.size());
        }
    }
    private static final class InMemoryRoles implements RoleRepository {
        public RoleCode nextId() { return new RoleCode("unused"); } public void save(Role value) {} public Optional<Role> findById(RoleCode id) { return Optional.empty(); }
        public Optional<Role> findByRoleCode(RoleCode code) { return Optional.empty(); } public boolean existsByRoleCode(RoleCode code) { return false; }
        public com.cloud.framework.domain.PagedList<Role> findRoles(int pageNo, int pageSize) {
            return com.cloud.framework.domain.PagedList.empty();
        }
    }
    private static final class InMemoryRoleGrants implements UserRoleGrantRepository {
        public GrantId nextId() { return new GrantId(1L); } public void save(UserRoleGrant value) {} public Optional<UserRoleGrant> findById(GrantId id) { return Optional.empty(); }
        public List<UserRoleGrant> findActiveByUserId(UserId id) { return List.of(); }
        public Optional<UserRoleGrant> findActiveByUserIdAndRoleCodeAndSource(UserId u, RoleCode r, GrantSource s) { return Optional.empty(); }
        public List<UserRoleGrant> findActiveByUserIdAndSource(UserId u, GrantSource s) { return List.of(); }
        public boolean existsActiveByUserIdAndRoleCodeAndSource(UserId u, RoleCode r, GrantSource s) { return false; }
    }
    private static final class InMemoryPermissionGrants implements UserPermissionGrantRepository {
        private final List<UserPermissionGrant> values = new ArrayList<>();
        public GrantId nextId() { return new GrantId((long) values.size() + 1); } public void save(UserPermissionGrant value) { values.add(value); }
        public Optional<UserPermissionGrant> findById(GrantId id) { return values.stream().filter(v -> v.id().equals(id)).findFirst(); }
        public List<UserPermissionGrant> findActiveByUserId(UserId id) { return values.stream().filter(v -> v.getUserId().equals(id)).toList(); }
        public Optional<UserPermissionGrant> findActiveByUserIdAndPermissionCodeAndSource(UserId u, PermissionCode c, GrantSource s) { return values.stream().filter(v -> v.getUserId().equals(u) && v.getPermissionCode().equals(c) && v.getSource().equals(s)).findFirst(); }
        public List<UserPermissionGrant> findActiveByUserIdAndSource(UserId u, GrantSource s) { return values.stream().filter(v -> v.getUserId().equals(u) && v.getSource().equals(s)).toList(); }
        public boolean existsActiveByUserIdAndPermissionCodeAndSource(UserId u, PermissionCode c, GrantSource s) { return findActiveByUserIdAndPermissionCodeAndSource(u, c, s).isPresent(); }
    }
}
