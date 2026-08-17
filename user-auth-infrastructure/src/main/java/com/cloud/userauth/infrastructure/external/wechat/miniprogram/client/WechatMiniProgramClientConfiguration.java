package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.cloud.userauth.infrastructure.external.wechat.miniprogram.config.WechatMiniProgramProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "user-auth.authentication.external-identity.wechat-mini-program",
        name = "enabled",
        havingValue = "true")
public class WechatMiniProgramClientConfiguration {
    private static final String WECHAT_API_BASE_URL = "https://api.weixin.qq.com";

    @Bean
    @ConditionalOnMissingBean
    WechatMiniProgramJsonMapper wechatMiniProgramJsonMapper(ObjectMapper objectMapper) {
        return new WechatMiniProgramJsonMapper(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    WechatMiniProgramResponseErrorHandler wechatMiniProgramResponseErrorHandler() {
        return new WechatMiniProgramResponseErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean(name = "wechatMiniProgramRestClient")
    RestClient wechatMiniProgramRestClient(
            RestClient.Builder builder,
            WechatMiniProgramProperties properties,
            WechatMiniProgramJsonMapper jsonMapper,
            WechatMiniProgramResponseErrorHandler errorHandler
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getRestClient().getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getRestClient().getReadTimeout());
        return builder.clone()
                .baseUrl(WECHAT_API_BASE_URL)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(jsonMapper.messageConverter());
                })
                .defaultStatusHandler(errorHandler)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    WechatMiniProgramApiClient wechatMiniProgramApiClient(
            @Qualifier("wechatMiniProgramRestClient")
            RestClient restClient,
            WechatMiniProgramProperties properties
    ) {
        return new RestClientWechatMiniProgramApiClient(
                restClient,
                properties);
    }

    @Bean
    @ConditionalOnMissingBean
    WechatMiniProgramAccessTokenProvider
            wechatMiniProgramAccessTokenProvider(
            WechatMiniProgramApiClient apiClient,
            WechatMiniProgramProperties properties,
            Clock clock
    ) {
        return new WechatMiniProgramAccessTokenProvider(
                apiClient,
                properties,
                clock);
    }

    @Bean
    @ConditionalOnMissingBean
    WechatMiniProgramClient wechatMiniProgramClient(
            WechatMiniProgramApiClient apiClient,
            WechatMiniProgramAccessTokenProvider accessTokenProvider
    ) {
        return new DefaultWechatMiniProgramClient(
                apiClient,
                accessTokenProvider);
    }
}
