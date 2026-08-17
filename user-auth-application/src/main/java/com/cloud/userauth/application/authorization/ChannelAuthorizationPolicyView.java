package com.cloud.userauth.application.authorization;

import java.time.Instant;
import java.util.List;

public record ChannelAuthorizationPolicyView(
        String channelCode,
        String status,
        Long version,
        List<String> roleCodes,
        List<String> directPermissionCodes,
        Instant createdAt,
        Instant updatedAt,
        Instant activatedAt
) {
    public ChannelAuthorizationPolicyView {
        roleCodes = List.copyOf(roleCodes);
        directPermissionCodes = List.copyOf(directPermissionCodes);
    }
}
