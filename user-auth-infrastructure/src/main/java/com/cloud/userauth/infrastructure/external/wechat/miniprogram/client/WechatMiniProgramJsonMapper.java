package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public final class WechatMiniProgramJsonMapper {
    private final ObjectMapper objectMapper;

    WechatMiniProgramJsonMapper(ObjectMapper source) {
        this.objectMapper = source.copy();
    }

    MappingJackson2HttpMessageConverter messageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        List<MediaType> supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        return converter;
    }
}
