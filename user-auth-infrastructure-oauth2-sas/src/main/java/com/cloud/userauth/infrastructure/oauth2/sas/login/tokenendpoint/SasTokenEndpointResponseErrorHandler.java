package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * 将 SAS Token Endpoint 的 OAuth2 错误响应转换为应用错误。
 */
@RequiredArgsConstructor
public final class SasTokenEndpointResponseErrorHandler implements ResponseErrorHandler {
    private final SasTokenEndpointJsonMapper jsonMapper;

    private static final Set<String> REJECT_ERROR = Set.of(
            OAuth2ErrorCodes.INVALID_GRANT, OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ErrorCodes.INVALID_SCOPE);

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) {
        SasErrorPayload errorResponse;
        try {
            errorResponse = jsonMapper.readValue(response.getBody(), SasErrorPayload.class);
        } catch (IOException exception) {
            throw new ApplicationException(ApplicationError.APP_TOKEN_ISSUE_FAILED, exception);
        }
        if (errorResponse != null && REJECT_ERROR.contains(errorResponse.error())) {
            throw new ApplicationException(ApplicationError.APP_LOGIN_REJECTED);
        }
        throw new ApplicationException(ApplicationError.APP_TOKEN_ISSUE_FAILED);
    }
}
