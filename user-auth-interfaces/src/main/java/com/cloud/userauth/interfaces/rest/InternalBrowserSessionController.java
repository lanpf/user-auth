package com.cloud.userauth.interfaces.rest;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommandOutput;
import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.userauth.api.facade.BrowserSessionCommandFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供网关逐请求调用的浏览器会话在线验证入口；仅面向可信内网，不对外部流量暴露。
 */
@RestController
@RequiredArgsConstructor
public class InternalBrowserSessionController {
    private final BrowserSessionCommandFacade facade;

    @PostMapping(UserAuthPathApiConstants.INTERNAL_BROWSER_SESSIONS_VERIFY)
    public Result<VerifyBrowserSessionApiCommandOutput> verify(
            @Valid @RequestBody VerificationRequest request
    ) {
        return facade.verify(new VerifyBrowserSessionApiCommand(request.getCredential()));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VerificationRequest {
        @NotBlank
        private String credential;
    }

}
