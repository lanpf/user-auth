package com.cloud.userauth.api.authorization;

import java.time.Instant;
import java.util.List;

public record ChannelAuthorizationPolicyApiResponse(
        String channelCode,
        String status,
        Long version,
        List<String> roleCodes,
        List<String> directPermissionCodes,
        Instant createdAt,
        Instant updatedAt,
        Instant activatedAt
) {
    public ChannelAuthorizationPolicyApiResponse {
        roleCodes = List.copyOf(roleCodes);
        directPermissionCodes = List.copyOf(directPermissionCodes);
    }
}
