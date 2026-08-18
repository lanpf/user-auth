package com.cloud.userauth.interfaces.rest;

import com.cloud.framework.core.AuthenticatedSessionRequest;
import com.cloud.framework.core.ClientRequest;
import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.enums.SessionHandoffTargetApiEnum;
import com.cloud.userauth.api.facade.SessionHandoffCommandFacade;
import com.cloud.userauth.interfaces.security.BrowserSessionCookie;
import com.cloud.userauth.interfaces.mapper.SessionHandoffRestMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    public Result<CreateSessionHandoffApiCommandOutput> create(
            @Valid @RequestBody CreateRequest request
    ) {
        return facade.create(mapper.toCommand(request));
    }

    @PostMapping(UserAuthRestPaths.API_WEB_VIEW_HANDOFFS_EXCHANGE)
    public Result<ExchangeRepresentation> exchange(
            @Valid @RequestBody ExchangeRequest request,
            HttpServletResponse response
    ) {
        Result<ExchangeSessionHandoffApiCommandOutput> result =
                facade.exchange(mapper.toCommand(request));
        ExchangeSessionHandoffApiCommandOutput output = result.getData();
        BrowserSessionCookie.write(response, output.sessionCredential(), Duration.ofSeconds(output.expiresIn()));
        return Result.success(mapper.toRepresentation(output));
    }

    public record ExchangeRepresentation(String handoffId, long expiresIn) {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest extends AuthenticatedSessionRequest {
        @NotNull
        private SessionHandoffTargetApiEnum target;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExchangeRequest extends ClientRequest {
        @NotBlank
        private String ticket;

        @NotNull
        private SessionHandoffTargetApiEnum target;
    }
}
