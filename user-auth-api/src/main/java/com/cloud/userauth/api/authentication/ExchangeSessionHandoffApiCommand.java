package com.cloud.userauth.api.authentication;

import com.cloud.framework.core.Request;
import com.cloud.userauth.api.enums.SessionHandoffTargetApiEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExchangeSessionHandoffApiCommand(
        @NotBlank String ticket,
        @NotNull SessionHandoffTargetApiEnum target
) implements Request {
}
