package com.cloud.userauth.infrastructure.oauth2.sas.login.tokenendpoint;

import com.cloud.userauth.application.common.ApplicationError;
import com.cloud.userauth.application.common.ApplicationException;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantParameterConverter;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.mobileotp.MobileOtpGrantRequest;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantParameterConverter;
import com.cloud.userauth.infrastructure.oauth2.sas.grant.external.ExternalIdentityGrantRequest;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import jakarta.validation.Validator;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

/** 使用 Spring RestClient 调用本机 SAS Token Endpoint。 */
@RequiredArgsConstructor
public class RestClientSasTokenEndpointClient
        implements SasTokenEndpointClient {
    static final String BULKHEAD_NAME = "sasInternalTokenEndpoint";

    private final RestClient restClient;
    private final URI tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final Validator validator;

    @Override
    @Bulkhead(name = BULKHEAD_NAME, fallbackMethod = "bulkheadFull")
    public SasTokenEndpointPayload requestToken(
            MobileOtpGrantRequest request
    ) {
        return requestToken(MobileOtpGrantParameterConverter.toTokenRequestForm(request));
    }

    @Override
    @Bulkhead(name = BULKHEAD_NAME, fallbackMethod = "externalBulkheadFull")
    public SasTokenEndpointPayload requestExternalToken(
            ExternalIdentityGrantRequest request
    ) {
        return requestToken(
                ExternalIdentityGrantParameterConverter.toTokenRequestForm(request));
    }

    @Override
    @Bulkhead(name = BULKHEAD_NAME, fallbackMethod = "refreshBulkheadFull")
    public SasRefreshTokenEndpointPayload requestRefreshToken(String refreshToken) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        SasRefreshTokenEndpointPayload response;
        try {
            response = restClient.post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(
                            clientId,
                            clientSecret,
                            StandardCharsets.UTF_8))
                    .body(form)
                    .retrieve()
                    .body(SasRefreshTokenEndpointPayload.class);
        } catch (ApplicationException | RestClientException exception) {
            throw new ApplicationException(ApplicationError.APP_REFRESH_TOKEN_FAILED, exception);
        }
        if (response == null || !validator.validate(response).isEmpty()) {
            throw new ApplicationException(ApplicationError.APP_REFRESH_TOKEN_FAILED);
        }
        return response;
    }

    private SasTokenEndpointPayload requestToken(
            MultiValueMap<String, String> form
    ) {
        SasTokenEndpointPayload response;
        try {
            response = restClient.post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(
                            clientId,
                            clientSecret,
                            StandardCharsets.UTF_8))
                    .body(form)
                    .retrieve()
                    .body(SasTokenEndpointPayload.class);
        } catch (RestClientException exception) {
            throw new ApplicationException(
                    ApplicationError.APP_TOKEN_ISSUE_FAILED,
                    exception);
        }
        if (response == null || !validator.validate(response).isEmpty()) {
            throw new ApplicationException(
                    ApplicationError.APP_TOKEN_ISSUE_FAILED);
        }
        return response;
    }

    private SasTokenEndpointPayload bulkheadFull(
            MobileOtpGrantRequest request,
            BulkheadFullException exception
    ) {
        throw new ApplicationException(
                ApplicationError.APP_INTERNAL_TOKEN_REQUEST_LIMIT_REACHED,
                exception);
    }

    private SasTokenEndpointPayload externalBulkheadFull(
            ExternalIdentityGrantRequest request,
            BulkheadFullException exception
    ) {
        throw new ApplicationException(
                ApplicationError.APP_INTERNAL_TOKEN_REQUEST_LIMIT_REACHED,
                exception);
    }

    private SasRefreshTokenEndpointPayload refreshBulkheadFull(
            String refreshToken,
            BulkheadFullException exception
    ) {
        throw new ApplicationException(
                ApplicationError.APP_INTERNAL_TOKEN_REQUEST_LIMIT_REACHED,
                exception);
    }
}
