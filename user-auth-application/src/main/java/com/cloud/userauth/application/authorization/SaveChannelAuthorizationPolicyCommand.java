package com.cloud.userauth.application.authorization;

import java.util.List;

public record SaveChannelAuthorizationPolicyCommand(
        String channelCode,
        Long expectedVersion,
        List<String> roleCodes,
        List<String> directPermissionCodes
) {
    public SaveChannelAuthorizationPolicyCommand {
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
        directPermissionCodes = directPermissionCodes == null
                ? List.of()
                : List.copyOf(directPermissionCodes);
    }
}
