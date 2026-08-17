package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.Getter;

@Getter
public class UserRoleGrant implements AggregateRoot<GrantId> {
    private final GrantId id;
    private final UserId userId;
    private final RoleCode roleCode;
    private final GrantSource source;
    private GrantStatus status;
    private final UserId grantedBy;
    private final Instant grantedAt;
    private final Instant expiresAt;
    private Instant updatedAt;

    private UserRoleGrant(
            GrantId id,
            UserId userId,
            RoleCode roleCode,
            GrantSource source,
            GrantStatus status,
            UserId grantedBy,
            Instant grantedAt,
            Instant expiresAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.roleCode = roleCode;
        this.source = source;
        this.status = status;
        this.grantedBy = grantedBy;
        this.grantedAt = grantedAt;
        this.expiresAt = expiresAt;
        this.updatedAt = updatedAt;
    }

    public static UserRoleGrant grant(
            GrantId id,
            UserId userId,
            RoleCode roleCode,
            GrantSource source,
            UserId grantedBy,
            Instant grantedAt,
            Instant expiresAt
    ) {
        return new UserRoleGrant(
                id, userId, roleCode, source, GrantStatus.ACTIVE,
                grantedBy, grantedAt, expiresAt, grantedAt);
    }

    public static UserRoleGrant restore(
            GrantId id,
            UserId userId,
            RoleCode roleCode,
            GrantSource source,
            GrantStatus status,
            UserId grantedBy,
            Instant grantedAt,
            Instant expiresAt,
            Instant updatedAt
    ) {
        return new UserRoleGrant(
                id, userId, roleCode, source, status,
                grantedBy, grantedAt, expiresAt, updatedAt);
    }

    public boolean isActiveAt(Instant currentTime) {
        if (status != GrantStatus.ACTIVE) {
            return false;
        }
        return expiresAt == null || currentTime.isBefore(expiresAt);
    }

    @Override
    public GrantId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public RoleCode roleCode() {
        return roleCode;
    }

    public void revoke(Instant revokedAt) {
        this.status = GrantStatus.REVOKED;
        this.updatedAt = revokedAt;
    }

    public void expire(Instant expiredAt) {
        if (status == GrantStatus.ACTIVE) {
            this.status = GrantStatus.EXPIRED;
            this.updatedAt = expiredAt;
        }
    }
}
