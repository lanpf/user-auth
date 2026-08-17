package com.cloud.userauth.infrastructure.external.wechat.miniprogram.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloud.userauth.infrastructure.common.InfrastructureError;
import com.cloud.userauth.infrastructure.common.InfrastructureException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;

class WechatMiniProgramResponseErrorHandlerTest {
    private final WechatMiniProgramResponseErrorHandler errorHandler =
            new WechatMiniProgramResponseErrorHandler();

    @Test
    void shouldOnlyClassifyHttpErrorStatusAsTransportError()
            throws Exception {
        assertFalse(errorHandler.hasError(
                new MockClientHttpResponse(new byte[0], HttpStatus.OK)));
        assertTrue(errorHandler.hasError(
                new MockClientHttpResponse(
                        new byte[0],
                        HttpStatus.BAD_GATEWAY)));
    }

    @Test
    void shouldTranslateHttpErrorToProviderUnavailable() {
        InfrastructureException exception = assertThrows(
                InfrastructureException.class,
                () -> errorHandler.handleError(
                        URI.create("https://api.weixin.qq.com/test"),
                        HttpMethod.GET,
                        new MockClientHttpResponse(
                                new byte[0],
                                HttpStatus.BAD_GATEWAY)));

        assertEquals(
                InfrastructureError.INFRA_ACL_EXTERNAL_IDENTITY_PROVIDER_UNAVAILABLE.errorCode(),
                exception.getErrorCode());
    }
}
