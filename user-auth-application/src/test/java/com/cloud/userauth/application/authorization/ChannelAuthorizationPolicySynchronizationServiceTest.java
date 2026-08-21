package com.cloud.userauth.application.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.framework.domain.DomainEventId;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyRepository;
import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.GrantSourceType;
import com.cloud.userauth.domain.authorization.GrantStatus;
import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.PermissionRepository;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.RoleRepository;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplication;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplicationRepository;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserPermissionGrantRepository;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.authorization.service.AuthorizationDomainService;
import com.cloud.userauth.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class ChannelAuthorizationPolicySynchronizationServiceTest {
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void shouldSynchronizeOnlyTheCurrentChannelSourceAndRemainIdempotent() {
        UserId userId = new UserId(10L);
        RoleCode roleCode = new RoleCode("buyer");
        PermissionCode rolePermission = new PermissionCode("order:view");
        PermissionCode directPermission = new PermissionCode("order:refund");
        Roles roles = new Roles(Role.create(roleCode, "Buyer", List.of(rolePermission), NOW));
        Permissions permissions = new Permissions(
                Permission.register(rolePermission, "View order", "order", NOW),
                Permission.register(directPermission, "Refund order", "order", NOW));
        RoleGrants roleGrants = new RoleGrants();
        PermissionGrants permissionGrants = new PermissionGrants();
        Policies policies = new Policies();
        Applications applications = new Applications();
        AtomicLong eventIds = new AtomicLong();
        AuthorizationDomainService domainService = new AuthorizationDomainService(
                roles, roleGrants, permissions, permissionGrants,
                () -> new DomainEventId(eventIds.incrementAndGet()));
        ChannelAuthorizationPolicy policy = ChannelAuthorizationPolicy.draft(
                new ChannelCode("PARTNER_A"), List.of(roleCode), List.of(directPermission), NOW);
        policy.activate(1L, NOW);
        policies.save(policy);
        GrantSource manual = new GrantSource(GrantSourceType.MANUAL, "approval-1");
        roleGrants.save(UserRoleGrant.grant(
                roleGrants.nextId(), userId, roleCode, manual, null, NOW, null));
        ChannelAuthorizationPolicySynchronizationService synchronizationService =
                new ChannelAuthorizationPolicySynchronizationService(
                policies, applications, roleGrants, permissionGrants,
                domainService, ignored -> { }, Clock.fixed(NOW, ZoneOffset.UTC));

        synchronizationService.synchronize(userId, new ChannelCode("PARTNER_A"));
        synchronizationService.synchronize(userId, new ChannelCode("PARTNER_A"));

        assertEquals(2, roleGrants.findActiveByUserId(userId).size());
        assertEquals(1, permissionGrants.findActiveByUserId(userId).size());
        assertEquals(2L, applications.findByUserIdAndChannelCode(
                userId, new ChannelCode("PARTNER_A")).orElseThrow().getAppliedVersion());

        applications.save(UserChannelPolicyApplication.firstApplied(
                new UserId(30L), new ChannelCode("PARTNER_A"), 2L, NOW));
        applications.save(UserChannelPolicyApplication.firstApplied(
                new UserId(20L), new ChannelCode("PARTNER_A"), 2L, NOW));
        applications.clearSaveOrder();

        policy.replaceTargets(2L, List.of(), List.of(), NOW.plusSeconds(1));
        policies.save(policy);
        ReconcileChannelAuthorizationPolicyOutput output = synchronizationService
                .synchronizePendingUsers(new ReconcileChannelAuthorizationPolicyCommand("PARTNER_A", 2));

        assertEquals(List.of(manual), roleGrants.findActiveByUserId(userId).stream()
                .map(UserRoleGrant::getSource)
                .toList());
        assertTrue(permissionGrants.findActiveByUserId(userId).isEmpty());
        assertEquals(2, output.processedUserCount());
        assertEquals(3L, output.policyVersion());
        assertTrue(output.hasPendingUsers());
        assertEquals(List.of(10L, 20L), applications.saveOrder());

        ReconcileChannelAuthorizationPolicyOutput resumed = synchronizationService
                .synchronizePendingUsers(new ReconcileChannelAuthorizationPolicyCommand("PARTNER_A", 2));
        assertEquals(1, resumed.processedUserCount());
        assertTrue(!resumed.hasPendingUsers());
        assertEquals(List.of(10L, 20L, 30L), applications.saveOrder());
    }

    private static final class Policies implements ChannelAuthorizationPolicyRepository {
        private final Map<ChannelCode, ChannelAuthorizationPolicy> values = new LinkedHashMap<>();
        @Override public void save(ChannelAuthorizationPolicy value) { values.put(value.getId(), value); }
        @Override public Optional<ChannelAuthorizationPolicy> findByChannelCode(ChannelCode id) { return Optional.ofNullable(values.get(id)); }
        @Override public boolean updateIfVersionMatches(ChannelAuthorizationPolicy value, Long expectedVersion) {
            ChannelAuthorizationPolicy current = values.get(value.getId());
            if (current == null || !expectedVersion.equals(current.getVersion() - 1)) {
                return false;
            }
            values.put(value.getId(), value);
            return true;
        }
        @Override public List<ChannelAuthorizationPolicy> findAll() { return List.copyOf(values.values()); }
    }

    private static final class Applications implements UserChannelPolicyApplicationRepository {
        private final List<UserChannelPolicyApplication> values = new ArrayList<>();
        private final List<Long> saveOrder = new ArrayList<>();
        @Override public void save(UserChannelPolicyApplication value) {
            values.removeIf(existing -> existing.getUserId().equals(value.getUserId())
                    && existing.getChannelCode().equals(value.getChannelCode()));
            values.add(value);
            saveOrder.add(value.getUserId().value());
        }
        void clearSaveOrder() { saveOrder.clear(); }
        List<Long> saveOrder() { return List.copyOf(saveOrder); }
        @Override public Optional<UserChannelPolicyApplication> findByUserIdAndChannelCode(UserId userId, ChannelCode channelCode) {
            return values.stream().filter(value -> value.getUserId().equals(userId)
                    && value.getChannelCode().equals(channelCode)).findFirst();
        }
        @Override public List<UserChannelPolicyApplication> findPendingByChannelCodeAndPolicyVersion(
                ChannelCode channelCode, Long policyVersion, int batchSize
        ) {
            return values.stream()
                    .filter(value -> value.getChannelCode().equals(channelCode))
                    .filter(value -> value.getAppliedVersion() < policyVersion)
                    .sorted(java.util.Comparator.comparing(value -> value.getUserId().value()))
                    .limit(batchSize)
                    .toList();
        }
    }

    private static final class Roles implements RoleRepository {
        private final Map<RoleCode, Role> values = new LinkedHashMap<>();
        private Roles(Role... roles) { for (Role role : roles) save(role); }
        @Override public RoleCode nextId() { throw new UnsupportedOperationException(); }
        @Override public void save(Role value) { values.put(value.getId(), value); }
        @Override public Optional<Role> findById(RoleCode id) { return Optional.ofNullable(values.get(id)); }
        @Override public Optional<Role> findByRoleCode(RoleCode code) { return findById(code); }
        @Override public boolean existsByRoleCode(RoleCode code) { return values.containsKey(code); }
        @Override public com.cloud.framework.domain.PagedList<Role> findRoles(int pageNo, int pageSize) {
            return new com.cloud.framework.domain.PagedList<>(List.copyOf(values.values()), (long) values.size());
        }
    }

    private static final class Permissions implements PermissionRepository {
        private final Map<PermissionCode, Permission> values = new LinkedHashMap<>();
        private Permissions(Permission... permissions) { for (Permission permission : permissions) save(permission); }
        @Override public PermissionCode nextId() { throw new UnsupportedOperationException(); }
        @Override public void save(Permission value) { values.put(value.getId(), value); }
        @Override public Optional<Permission> findById(PermissionCode id) { return Optional.ofNullable(values.get(id)); }
        @Override public List<Permission> findByIds(Collection<PermissionCode> ids) {
            return ids.stream().map(values::get).filter(java.util.Objects::nonNull).toList();
        }
        @Override public com.cloud.framework.domain.PagedList<Permission> findPermissions(int pageNo, int pageSize) {
            return new com.cloud.framework.domain.PagedList<>(List.copyOf(values.values()), (long) values.size());
        }
    }

    private static final class RoleGrants implements UserRoleGrantRepository {
        private final AtomicLong ids = new AtomicLong();
        private final Map<GrantId, UserRoleGrant> values = new LinkedHashMap<>();
        @Override public GrantId nextId() { return new GrantId(ids.incrementAndGet()); }
        @Override public void save(UserRoleGrant value) { values.put(value.id(), value); }
        @Override public Optional<UserRoleGrant> findById(GrantId id) { return Optional.ofNullable(values.get(id)); }
        @Override public List<UserRoleGrant> findActiveByUserId(UserId userId) {
            return values.values().stream().filter(value -> value.userId().equals(userId)
                    && value.getStatus() == GrantStatus.ACTIVE).toList();
        }
        @Override public Optional<UserRoleGrant> findActiveByUserIdAndRoleCodeAndSource(UserId userId, RoleCode roleCode, GrantSource source) {
            return findActiveByUserId(userId).stream().filter(value -> value.roleCode().equals(roleCode)
                    && value.getSource().equals(source)).findFirst();
        }
        @Override public List<UserRoleGrant> findActiveByUserIdAndSource(UserId userId, GrantSource source) {
            return findActiveByUserId(userId).stream().filter(value -> value.getSource().equals(source)).toList();
        }
        @Override public boolean existsActiveByUserIdAndRoleCodeAndSource(UserId userId, RoleCode roleCode, GrantSource source) {
            return findActiveByUserIdAndRoleCodeAndSource(userId, roleCode, source).isPresent();
        }
    }

    private static final class PermissionGrants implements UserPermissionGrantRepository {
        private final AtomicLong ids = new AtomicLong();
        private final Map<GrantId, UserPermissionGrant> values = new LinkedHashMap<>();
        @Override public GrantId nextId() { return new GrantId(ids.incrementAndGet()); }
        @Override public void save(UserPermissionGrant value) { values.put(value.id(), value); }
        @Override public Optional<UserPermissionGrant> findById(GrantId id) { return Optional.ofNullable(values.get(id)); }
        @Override public List<UserPermissionGrant> findActiveByUserId(UserId userId) {
            return values.values().stream().filter(value -> value.getUserId().equals(userId)
                    && value.getStatus() == GrantStatus.ACTIVE).toList();
        }
        @Override public Optional<UserPermissionGrant> findActiveByUserIdAndPermissionCodeAndSource(UserId userId, PermissionCode permissionCode, GrantSource source) {
            return findActiveByUserId(userId).stream().filter(value -> value.getPermissionCode().equals(permissionCode)
                    && value.getSource().equals(source)).findFirst();
        }
        @Override public List<UserPermissionGrant> findActiveByUserIdAndSource(UserId userId, GrantSource source) {
            return findActiveByUserId(userId).stream().filter(value -> value.getSource().equals(source)).toList();
        }
        @Override public boolean existsActiveByUserIdAndPermissionCodeAndSource(UserId userId, PermissionCode permissionCode, GrantSource source) {
            return findActiveByUserIdAndPermissionCodeAndSource(userId, permissionCode, source).isPresent();
        }
    }
}
