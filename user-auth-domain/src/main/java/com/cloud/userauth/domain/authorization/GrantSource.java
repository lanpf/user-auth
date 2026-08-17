package com.cloud.userauth.domain.authorization;

import com.cloud.userauth.domain.common.DomainError;
import com.cloud.userauth.domain.common.DomainException;
import java.util.Objects;

/**
 * 授予角色或直接权限的可追溯来源。
 * sourceId 在 sourceType 内唯一，例如渠道授权策略使用 channelCode。
 */
public record GrantSource(GrantSourceType sourceType, String sourceId) {
    public GrantSource {
        Objects.requireNonNull(sourceType, "sourceType");
        if (sourceId == null || sourceId.trim().isEmpty()) {
            throw new DomainException(DomainError.DOMAIN_FIELD_REQUIRED);
        }
        sourceId = sourceId.trim();
    }
}
