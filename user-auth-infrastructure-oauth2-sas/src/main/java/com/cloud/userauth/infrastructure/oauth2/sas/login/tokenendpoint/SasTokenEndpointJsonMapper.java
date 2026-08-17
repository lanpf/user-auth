package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/** SAS Token Endpoint 专用的 snake_case JSON 映射策略。 */
public final class SasTokenEndpointJsonMapper {
    private final ObjectMapper objectMapper;

    public SasTokenEndpointJsonMapper(ObjectMapper source) {
        this.objectMapper = source.copy()
                .setPropertyNamingStrategy(
                        PropertyNamingStrategies.SNAKE_CASE);
    }

    public <T> T readValue(InputStream input, Class<T> type) throws IOException {
        return objectMapper.readValue(input, type);
    }

    public MappingJackson2HttpMessageConverter messageConverter() {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
