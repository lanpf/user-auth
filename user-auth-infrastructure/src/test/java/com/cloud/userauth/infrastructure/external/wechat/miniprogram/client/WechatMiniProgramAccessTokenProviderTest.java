package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramCode2SessionPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramPhoneNumberPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.client.payload.WechatMiniProgramStableAccessTokenPayload;
import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class WechatMiniProgramAccessTokenProviderTest {
    @Test
    void shouldCacheAndRefreshStableAccessToken() {
        CountingApiClient apiClient = new CountingApiClient();
        WechatMiniProgramProperties properties =
                new WechatMiniProgramProperties();
        properties.setAccessTokenRefreshSkew(Duration.ofSeconds(60));
        MutableClock clock = new MutableClock(
                Instant.parse("2026-07-29T08:00:00Z"));
        WechatMiniProgramAccessTokenProvider provider =
                new WechatMiniProgramAccessTokenProvider(
                        apiClient,
                        properties,
                        clock);

        assertEquals("access-token-1", provider.getAccessToken());
        assertEquals("access-token-1", provider.getAccessToken());
        assertEquals(1, apiClient.accessTokenRequests);

        clock.advance(Duration.ofSeconds(61));

        assertEquals("access-token-2", provider.getAccessToken());
        assertEquals(2, apiClient.accessTokenRequests);
    }

    private static final class CountingApiClient
            implements WechatMiniProgramApiClient {
        private int accessTokenRequests;

        @Override
        public WechatMiniProgramCode2SessionPayload exchangeLoginCode(
                String loginCode
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public WechatMiniProgramStableAccessTokenPayload getStableAccessToken() {
            accessTokenRequests++;
            return new WechatMiniProgramStableAccessTokenPayload(
                    "access-token-" + accessTokenRequests,
                    120L,
                    null,
                    null);
        }

        @Override
        public WechatMiniProgramPhoneNumberPayload exchangePhoneCode(
                String accessToken,
                String phoneCode
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
