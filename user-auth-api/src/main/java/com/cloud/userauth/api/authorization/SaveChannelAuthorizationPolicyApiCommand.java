package com.cloud.userauth.api.authorization;

import com.cloud.framework.core.Request;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SaveChannelAuthorizationPolicyApiCommand(
        @NotBlank String channelCode,
        Long expectedVersion,
        List<String> roleCodes,
        List<String> directPermissionCodes
) implements Request {
    public SaveChannelAuthorizationPolicyApiCommand {
        roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
        directPermissionCodes = directPermissionCodes == null
                ? List.of()
                : List.copyOf(directPermissionCodes);
    }
}
