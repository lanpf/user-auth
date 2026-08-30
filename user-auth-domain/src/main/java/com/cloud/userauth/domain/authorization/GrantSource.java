package com.cloud.userauth.domain.authorization;

import com.cloud.framework.core.validation.Require;
import com.cloud.userauth.domain.common.DomainException;

/**
 * 授予角色或直接权限的可追溯来源。
 * sourceId 在 sourceType 内唯一，例如渠道授权策略使用 channelCode。
 */
public record GrantSource(GrantSourceType sourceType, String sourceId) {
    public GrantSource {
        Require.notNull(sourceType, DomainException::missingField);
        sourceId = Require.notBlank(sourceId, DomainException::missingField).trim();
    }
}
