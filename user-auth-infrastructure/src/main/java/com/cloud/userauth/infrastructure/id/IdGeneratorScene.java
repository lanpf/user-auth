package com.cloud.userauth.infrastructure.id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdGeneratorScene {
    AUTH_ACCOUNT("auth_account"),
    USER("user"),
    CREDENTIAL("credential"),
    AUTH_CHALLENGE("auth_challenge"),
    REGISTRATION_PROCESS("registration_process"),
    USER_ROLE_GRANT("user_role_grant"),
    USER_PERMISSION_GRANT("user_permission_grant");

    private final String value;
}
