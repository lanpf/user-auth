package com.cloud.userauth.domain.authorization;

import com.cloud.framework.core.validation.Require;
import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public class ChannelAuthorizationPolicy implements AggregateRoot<ChannelCode> {
    private final ChannelCode channelCode;
    private ChannelAuthorizationPolicyStatus status;
    private Long version;
    private List<RoleCode> roleCodes;
    private List<PermissionCode> directPermissionCodes;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant activatedAt;

    private ChannelAuthorizationPolicy(
            ChannelCode channelCode,
            ChannelAuthorizationPolicyStatus status,
            Long version,
            List<RoleCode> roleCodes,
            List<PermissionCode> directPermissionCodes,
            Instant createdAt,
            Instant updatedAt,
            Instant activatedAt
    ) {
        this.channelCode = channelCode;
        this.status = status;
        this.version = version;
        this.roleCodes = distinct(roleCodes);
        this.directPermissionCodes = distinct(directPermissionCodes);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.activatedAt = activatedAt;
    }

    public static ChannelAuthorizationPolicy draft(
            ChannelCode channelCode,
            List<RoleCode> roleCodes,
            List<PermissionCode> directPermissionCodes,
            Instant createdAt
    ) {
        return new ChannelAuthorizationPolicy(
                channelCode,
                ChannelAuthorizationPolicyStatus.DRAFT,
                1L,
                roleCodes,
                directPermissionCodes,
                createdAt,
                createdAt,
                null);
    }

    public static ChannelAuthorizationPolicy restore(
            ChannelCode channelCode,
            ChannelAuthorizationPolicyStatus status,
            Long version,
            List<RoleCode> roleCodes,
            List<PermissionCode> directPermissionCodes,
            Instant createdAt,
            Instant updatedAt,
            Instant activatedAt
    ) {
        return new ChannelAuthorizationPolicy(
                channelCode, status, version, roleCodes, directPermissionCodes,
                createdAt, updatedAt, activatedAt);
    }

    public void replaceTargets(
            Long expectedVersion,
            List<RoleCode> newRoleCodes,
            List<PermissionCode> newDirectPermissionCodes,
            Instant changedAt
    ) {
        assertVersion(expectedVersion);
        roleCodes = distinct(newRoleCodes);
        directPermissionCodes = distinct(newDirectPermissionCodes);
        version++;
        updatedAt = changedAt;
    }

    public void activate(Long expectedVersion, Instant changedAt) {
        assertVersion(expectedVersion);
        status = ChannelAuthorizationPolicyStatus.ACTIVE;
        version++;
        updatedAt = changedAt;
        activatedAt = changedAt;
    }

    public void disable(Long expectedVersion, Instant changedAt) {
        assertVersion(expectedVersion);
        status = ChannelAuthorizationPolicyStatus.DISABLED;
        version++;
        updatedAt = changedAt;
    }

    public boolean isActive() {
        return status == ChannelAuthorizationPolicyStatus.ACTIVE;
    }

    @Override
    public ChannelCode getId() {
        return channelCode;
    }

    private void assertVersion(Long expectedVersion) {
        if (!version.equals(expectedVersion)) {
            throw new DomainException(DomainError.CHANNEL_AUTHORIZATION_POLICY_VERSION_CONFLICT);
        }
    }

    private static <T> List<T> distinct(List<T> values) {
        return Require.notNull(values, DomainException::missingField).stream().distinct().toList();
    }
}
