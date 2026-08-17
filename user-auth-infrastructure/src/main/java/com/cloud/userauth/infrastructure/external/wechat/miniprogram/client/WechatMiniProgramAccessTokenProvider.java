package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
final class WechatMiniProgramAccessTokenProvider {
    private final WechatMiniProgramApiClient apiClient;
    private final WechatMiniProgramProperties properties;
    private final Clock clock;

    private volatile CachedAccessToken cachedToken;

    String getAccessToken() {
        Instant now = clock.instant();
        CachedAccessToken current = cachedToken;
        if (current != null && current.isUsableAt(now)) {
            return current.value();
        }
        synchronized (this) {
            now = clock.instant();
            current = cachedToken;
            if (current != null && current.isUsableAt(now)) {
                return current.value();
            }
            WechatStableAccessTokenPayload response = apiClient.getStableAccessToken();
            if (!StringUtils.hasText(response.accessToken())
                    || response.expiresIn() == null
                    || response.expiresIn() <= 0) {
                throw new InfrastructureException(
                        InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE);
            }
            long usableSeconds = Math.max(
                    1,
                    response.expiresIn()
                            - properties.getAccessTokenRefreshSkew().toSeconds());
            cachedToken = new CachedAccessToken(
                    response.accessToken(),
                    now.plusSeconds(usableSeconds));
            return cachedToken.value();
        }
    }

    private record CachedAccessToken(String value, Instant refreshAt) {
        boolean isUsableAt(Instant now) {
            return now.isBefore(refreshAt);
        }
    }
}
