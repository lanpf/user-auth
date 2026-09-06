package com.cloud.userauth.interfaces.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.EndBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommandOutput;
import com.cloud.userauth.api.facade.BrowserSessionCommandFacade;
import com.cloud.userauth.application.session.browser.EndBrowserSessionCommand;
import com.cloud.userauth.application.session.browser.EndBrowserSessionCommandService;
import com.cloud.userauth.application.session.browser.VerifyBrowserSessionCommand;
import com.cloud.userauth.application.session.browser.VerifyBrowserSessionCommandService;
import com.cloud.userauth.application.session.browser.VerifyBrowserSessionOutput;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class DefaultBrowserSessionCommandFacade implements BrowserSessionCommandFacade {
    private final VerifyBrowserSessionCommandService verifyService;
    private final EndBrowserSessionCommandService endService;

    @Override
    public Result<VerifyBrowserSessionApiCommandOutput> verify(
            VerifyBrowserSessionApiCommand command
    ) {
        VerifyBrowserSessionOutput output = verifyService
                .execute(new VerifyBrowserSessionCommand(command.credential()));
        return Result.success(toApiOutput(output));
    }

    @Override
    public Result<Void> end(EndBrowserSessionApiCommand command) {
        endService.execute(new EndBrowserSessionCommand(
                new UserId(command.authenticatedUserId()),
                new SessionId(command.sessionId()),
                command.credential()));
        return Result.success();
    }

    private VerifyBrowserSessionApiCommandOutput toApiOutput(
            VerifyBrowserSessionOutput output
    ) {
        if (output instanceof VerifyBrowserSessionOutput.Verified verified) {
            return VerifyBrowserSessionApiCommandOutput.verified(
                    verified.userId().value(),
                    verified.sessionId().value(),
                    verified.remainingIdleTtl().toSeconds());
        }
        return VerifyBrowserSessionApiCommandOutput.unverified();
    }
}
