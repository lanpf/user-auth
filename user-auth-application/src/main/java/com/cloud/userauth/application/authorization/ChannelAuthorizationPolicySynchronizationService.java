package com.cloud.userauth.application.authorization;

import com.cloud.framework.domain.DomainEventStore;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyRepository;
import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.GrantSourceType;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplication;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplicationRepository;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserPermissionGrantRepository;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.authorization.service.AuthorizationDomainService;
import com.cloud.userauth.domain.authorization.service.RoleGrantEffect;
import com.cloud.userauth.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 根据渠道授权策略同步用户授权，并以已提交的用户版本游标恢复批处理重放。
 */
@RequiredArgsConstructor
public class ChannelAuthorizationPolicySynchronizationService
        implements UserChannelAuthorizationSynchronizer {
    private final ChannelAuthorizationPolicyRepository policyRepository;
    private final UserChannelPolicyApplicationRepository applicationRepository;
    private final UserRoleGrantRepository roleGrantRepository;
    private final UserPermissionGrantRepository permissionGrantRepository;
    private final AuthorizationDomainService authorizationDomainService;
    private final DomainEventStore domainEventStore;
    private final Clock clock;

    @Transactional
    @Override
    public void synchronize(UserId userId, ChannelCode channelCode) {
        ChannelAuthorizationPolicy policy = policyRepository.findByChannelCode(channelCode)
                .orElse(null);
        if (policy == null) {
            return;
        }
        UserChannelPolicyApplication application = applicationRepository
                .findByUserIdAndChannelCode(userId, channelCode)
                .orElse(null);
        if (!policy.isActive() && application == null) {
            return;
        }
        if (application != null && application.getAppliedVersion().equals(policy.getVersion())) {
            return;
        }
        synchronize(userId, policy, application, clock.instant());
    }

    @Transactional
    public ReconcileChannelAuthorizationPolicyOutput synchronizePendingUsers(
            ReconcileChannelAuthorizationPolicyCommand command
    ) {
        ChannelCode channelCode = new ChannelCode(command.channelCode());
        ChannelAuthorizationPolicy policy = policyRepository.findByChannelCode(channelCode)
                .orElseThrow();
        List<UserChannelPolicyApplication> applications =
                applicationRepository.findPendingByChannelCodeAndPolicyVersion(
                        channelCode, policy.getVersion(), command.batchSize());
        Instant now = clock.instant();
        applications.forEach(application ->
                synchronize(application.getUserId(), policy, application, now));
        boolean hasPendingUsers = applications.size() == command.batchSize()
                && !CollectionUtils.isEmpty(
                        applicationRepository.findPendingByChannelCodeAndPolicyVersion(
                                channelCode, policy.getVersion(), 1));
        return new ReconcileChannelAuthorizationPolicyOutput(
                applications.size(), policy.getVersion(), hasPendingUsers);
    }

    private void synchronize(
            UserId userId,
            ChannelAuthorizationPolicy policy,
            UserChannelPolicyApplication application,
            Instant now
    ) {
        GrantSource source = new GrantSource(
                GrantSourceType.CHANNEL_AUTHORIZATION_POLICY,
                policy.getChannelCode().value());
        Set<RoleCode> expectedRoles = policy.isActive()
                ? Set.copyOf(policy.getRoleCodes())
                : Set.of();
        Set<PermissionCode> expectedPermissions = policy.isActive()
                ? Set.copyOf(policy.getDirectPermissionCodes())
                : Set.of();
        synchronizeRoles(userId, source, expectedRoles, now);
        synchronizePermissions(userId, source, expectedPermissions, now);

        UserChannelPolicyApplication updated = application == null
                ? UserChannelPolicyApplication.firstApplied(
                        userId, policy.getChannelCode(), policy.getVersion(), now)
                : application;
        if (application != null) {
            updated.recordApplied(policy.getVersion(), now);
        }
        applicationRepository.save(updated);
    }

    private void synchronizeRoles(
            UserId userId,
            GrantSource source,
            Set<RoleCode> expected,
            Instant now
    ) {
        List<UserRoleGrant> current = roleGrantRepository.findActiveByUserIdAndSource(userId, source);
        Set<RoleCode> currentCodes = current.stream().map(UserRoleGrant::roleCode).collect(Collectors.toSet());
        expected.stream()
                .filter(roleCode -> !currentCodes.contains(roleCode))
                .forEach(roleCode -> {
                    RoleGrantEffect effect = authorizationDomainService.grantRole(
                            roleGrantRepository.nextId(), userId, roleCode, source,
                            null, now, null);
                    roleGrantRepository.save(effect.grant());
                    domainEventStore.appendAll(effect.events());
                });
        current.stream()
                .filter(grant -> !expected.contains(grant.roleCode()))
                .forEach(grant -> {
                    RoleGrantEffect effect = authorizationDomainService.revokeRole(
                            userId, grant.roleCode(), source, now);
                    roleGrantRepository.save(effect.grant());
                    domainEventStore.appendAll(effect.events());
                });
    }

    private void synchronizePermissions(
            UserId userId,
            GrantSource source,
            Set<PermissionCode> expected,
            Instant now
    ) {
        List<UserPermissionGrant> current =
                permissionGrantRepository.findActiveByUserIdAndSource(userId, source);
        Set<PermissionCode> currentCodes = current.stream().map(UserPermissionGrant::getPermissionCode).collect(Collectors.toSet());
        expected.stream()
                .filter(permissionCode -> !currentCodes.contains(permissionCode))
                .forEach(permissionCode -> {
                    UserPermissionGrant grant = authorizationDomainService.grantPermission(
                            userId, permissionCode, source, null, now, null,
                            GrantSourceType.CHANNEL_AUTHORIZATION_POLICY.name().toLowerCase());
                    permissionGrantRepository.save(grant);
                });
        current.stream()
                .filter(grant -> !expected.contains(grant.getPermissionCode()))
                .forEach(grant -> {
                    authorizationDomainService.revokePermission(
                            userId, grant.getPermissionCode(), source, now);
                    permissionGrantRepository.save(grant);
                });
    }
}
