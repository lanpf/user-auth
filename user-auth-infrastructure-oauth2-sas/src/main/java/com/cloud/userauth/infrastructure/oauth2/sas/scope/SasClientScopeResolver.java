package com.cloud.userauth.infrastructure.oauth2.sas.scope;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public interface SasClientScopeResolver {

    Set<String> resolve(@NotBlank String clientAppId);
}
