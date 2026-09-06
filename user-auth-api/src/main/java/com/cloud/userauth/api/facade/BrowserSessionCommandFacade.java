package com.cloud.userauth.api.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.EndBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommandOutput;
import jakarta.validation.Valid;

/** 浏览器会话的验证与生命周期命令。 */
public interface BrowserSessionCommandFacade {

    Result<VerifyBrowserSessionApiCommandOutput> verify(
            @Valid VerifyBrowserSessionApiCommand command);

    Result<Void> end(@Valid EndBrowserSessionApiCommand command);
}
