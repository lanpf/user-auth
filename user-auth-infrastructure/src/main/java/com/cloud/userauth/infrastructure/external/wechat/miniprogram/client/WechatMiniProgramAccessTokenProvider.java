package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramStableAccessTokenPayload;
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
            WechatMiniProgramStableAccessTokenPayload response;
            try {
                response = apiClient.getStableAccessToken();
            } catch (WechatMiniProgramApiException exception) {
                throw providerUnavailable(exception);
            }
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

    void invalidate(String accessToken) {
        synchronized (this) {
            CachedAccessToken current = cachedToken;
            if (current != null && current.value().equals(accessToken)) {
                cachedToken = null;
            }
        }
    }

    private static InfrastructureException providerUnavailable(Throwable cause) {
        return new InfrastructureException(
                InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE,
                cause);
    }

    private record CachedAccessToken(String value, Instant refreshAt) {
        boolean isUsableAt(Instant now) {
            return now.isBefore(refreshAt);
        }
    }
}
