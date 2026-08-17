package com.cloud.userauth.interfaces.rest;

import com.cloud.framework.core.ClientRequest;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.facade.SessionHandoffCommandFacade;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.interfaces.security.AuthenticatedSessionResolver;
import com.cloud.userauth.interfaces.security.H5SessionCookie;
import com.cloud.userauth.interfaces.mapper.SessionHandoffRestMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序与 H5 WebView 之间的一次性登录态交接协议。
 */
@RestController
@RequiredArgsConstructor
public class WebViewHandoffController {
    private final SessionHandoffCommandFacade facade;
    private final SessionHandoffRestMapper mapper;

    @PostMapping(UserAuthRestPaths.API_WEB_VIEW_HANDOFFS)
    public Result<CreateH5SessionHandoffApiCommandOutput> create(
            @Valid ClientRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        AuthenticatedSession authenticated = authenticatedSession(principal);
        return facade.createH5SessionHandoff(mapper.toCreateCommand(authenticated));
    }

    @PostMapping(UserAuthRestPaths.API_WEB_VIEW_HANDOFFS_EXCHANGE)
    public Result<ExchangeRepresentation> exchange(
            @Valid @RequestBody ExchangeRequest request,
            HttpServletResponse response
    ) {
        Result<ExchangeH5SessionHandoffApiCommandOutput> result =
                facade.exchangeH5SessionHandoff(mapper.toCommand(request));
        ExchangeH5SessionHandoffApiCommandOutput output = result.getData();
        H5SessionCookie.write(response, output.sessionCredential(), Duration.ofSeconds(output.expiresIn()));
        return Result.success(mapper.toRepresentation(output));
    }


    private static AuthenticatedSession authenticatedSession(Object principal) {
        return AuthenticatedSessionResolver.resolve(principal);
    }

    public record ExchangeRepresentation(String handoffId, long expiresIn) {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExchangeRequest extends ClientRequest {
        @NotBlank
        private String ticket;
    }
}
