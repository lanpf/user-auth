package com.cloud.userauth.domain.authorization;

import com.cloud.framework.domain.AggregateRoot;
import com.cloud.userauth.domain.user.UserId;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPermissionGrant implements AggregateRoot<GrantId> {
    private final GrantId id;
    private final UserId userId;
    private final PermissionCode permissionCode;
    private final GrantSource source;
    private GrantStatus status;
    private final UserId grantedBy;
    private final Instant grantedAt;
    private final Instant expiresAt;
    private final String reason;
    private Instant updatedAt;

    public static UserPermissionGrant grant(GrantId id, UserId userId, PermissionCode permissionCode, GrantSource source,
                                            UserId grantedBy, Instant grantedAt, Instant expiresAt, String reason) {
        return new UserPermissionGrant(id, userId, permissionCode, source, GrantStatus.ACTIVE,
                grantedBy, grantedAt, expiresAt, reason, grantedAt);
    }

    public static UserPermissionGrant restore(GrantId id, UserId userId, PermissionCode permissionCode, GrantSource source,
                                              GrantStatus status, UserId grantedBy, Instant grantedAt,
                                              Instant expiresAt, String reason, Instant updatedAt) {
        return new UserPermissionGrant(id, userId, permissionCode, source, status,
                grantedBy, grantedAt, expiresAt, reason, updatedAt);
    }

    public boolean isActiveAt(Instant at) { return status == GrantStatus.ACTIVE && (expiresAt == null || at.isBefore(expiresAt)); }
    public void revoke(Instant revokedAt) { status = GrantStatus.REVOKED; updatedAt = revokedAt; }
}
