package com.cloud.userauth.application.authorization;

import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyRepository;
import com.cloud.userauth.domain.authorization.ChannelCode;
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
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplicationRepository;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrantRepository;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.framework.core.PageQuery;
import com.cloud.framework.domain.PagedList;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AuthorizationQueryService {
    private final ChannelAuthorizationPolicyRepository policyRepository;
    private final UserChannelPolicyApplicationRepository applicationRepository;
    private final UserRoleGrantRepository roleGrantRepository;
    private final UserPermissionGrantRepository permissionGrantRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ChannelAuthorizationPolicyView findPolicy(String channelCode) {
        return policyRepository.findByChannelCode(new ChannelCode(channelCode))
                .map(ChannelAuthorizationPolicyCommandService::view)
                .orElseThrow(() -> new DomainException(
                        DomainError.CHANNEL_AUTHORIZATION_POLICY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserAuthorizationView findUserAuthorization(Long rawUserId) {
        UserId userId = new UserId(rawUserId);
        Instant now = clock.instant();
        List<UserRoleGrant> roleGrants = roleGrantRepository.findActiveByUserId(userId).stream()
                .filter(grant -> grant.isActiveAt(now))
                .filter(grant -> isCurrentChannelPolicyGrant(userId, grant.getSource()))
                .toList();
        List<UserPermissionGrant> permissionGrants =
                permissionGrantRepository.findActiveByUserId(userId).stream()
                        .filter(grant -> grant.isActiveAt(now))
                        .filter(grant -> isCurrentChannelPolicyGrant(userId, grant.getSource()))
                        .toList();
        Set<String> effectiveRoles = new LinkedHashSet<>();
        Set<String> effectivePermissions = new LinkedHashSet<>();
        roleGrants.forEach(grant -> roleRepository.findById(grant.roleCode())
                .filter(role -> role.status() == com.cloud.userauth.domain.authorization.RoleStatus.ACTIVE)
                .ifPresent(role -> {
                    effectiveRoles.add(role.getRoleCode().value());
                    role.permissionCodes().stream()
                            .filter(this::isActivePermission)
                            .map(PermissionCode::value)
                            .forEach(effectivePermissions::add);
                }));
        permissionGrants.stream()
                .map(UserPermissionGrant::getPermissionCode)
                .filter(this::isActivePermission)
                .map(PermissionCode::value)
                .forEach(effectivePermissions::add);
        return new UserAuthorizationView(
                rawUserId,
                List.copyOf(effectiveRoles),
                List.copyOf(effectivePermissions),
                roleGrants.stream().map(AuthorizationQueryService::view).toList(),
                permissionGrants.stream().map(AuthorizationQueryService::view).toList());
    }

    @Transactional(readOnly = true)
    public PermissionView findPermission(String rawCode) {
        return permissionRepository.findById(new PermissionCode(rawCode))
                .map(AuthorizationCatalogCommandService::view)
                .orElseThrow(() -> new DomainException(DomainError.PERMISSION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PagedList<PermissionView> findPermissions(PageQuery query) {
        PagedList<Permission> page = permissionRepository.findPermissions(
                query.zeroBasedPageNo(), query.pageSize());
        return new PagedList<>(page.getData().stream()
                .map(AuthorizationCatalogCommandService::view)
                .toList(), page.getTotal());
    }

    @Transactional(readOnly = true)
    public RoleView findRole(String rawCode) {
        return roleRepository.findById(new RoleCode(rawCode))
                .map(AuthorizationCatalogCommandService::view)
                .orElseThrow(() -> new DomainException(DomainError.ROLE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PagedList<RoleView> findRoles(PageQuery query) {
        PagedList<Role> page = roleRepository.findRoles(
                query.zeroBasedPageNo(), query.pageSize());
        return new PagedList<>(page.getData().stream()
                .map(AuthorizationCatalogCommandService::view)
                .toList(), page.getTotal());
    }

    private boolean isActivePermission(PermissionCode permissionCode) {
        return permissionRepository.findById(permissionCode)
                .map(Permission::isActive)
                .orElse(false);
    }

    private boolean isCurrentChannelPolicyGrant(UserId userId, GrantSource source) {
        if (source.sourceType() != GrantSourceType.CHANNEL_AUTHORIZATION_POLICY) {
            return true;
        }
        ChannelCode channelCode = new ChannelCode(source.sourceId());
        return policyRepository.findByChannelCode(channelCode)
                .filter(policy -> policy.isActive())
                .flatMap(policy -> applicationRepository.findByUserIdAndChannelCode(userId, channelCode)
                        .filter(application -> application.getAppliedVersion().equals(policy.getVersion())))
                .isPresent();
    }

    private static UserAuthorizationView.AuthorizationGrantView view(UserRoleGrant grant) {
        return new UserAuthorizationView.AuthorizationGrantView(
                grant.roleCode().value(),
                grant.getSource().sourceType().name(),
                grant.getSource().sourceId(),
                grant.getStatus().name());
    }

    private static UserAuthorizationView.AuthorizationGrantView view(UserPermissionGrant grant) {
        return new UserAuthorizationView.AuthorizationGrantView(
                grant.getPermissionCode().value(),
                grant.getSource().sourceType().name(),
                grant.getSource().sourceId(),
                grant.getStatus().name());
    }
}
