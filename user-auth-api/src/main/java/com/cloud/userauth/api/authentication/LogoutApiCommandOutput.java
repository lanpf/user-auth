package com.cloud.userauth.api.authentication;

import com.cloud.userauth.api.enums.LoginSessionStatusApiEnum;
import java.io.Serializable;

public record LogoutApiCommandOutput(
        String sessionId,
        LoginSessionStatusApiEnum sessionStatus
) implements Serializable {
}
