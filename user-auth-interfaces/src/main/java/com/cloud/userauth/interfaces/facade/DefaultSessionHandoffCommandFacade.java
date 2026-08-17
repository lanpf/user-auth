package com.cloud.userauth.interfaces.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.facade.SessionHandoffCommandFacade;
import com.cloud.userauth.application.session.handoff.H5SessionHandoffCommandService;
import com.cloud.userauth.interfaces.mapper.SessionHandoffApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class DefaultSessionHandoffCommandFacade implements SessionHandoffCommandFacade {
    private final H5SessionHandoffCommandService commandService;
    private final SessionHandoffApiMapper mapper;

    @Override
    public Result<CreateH5SessionHandoffApiCommandOutput> createH5SessionHandoff(
            CreateH5SessionHandoffApiCommand request
    ) {
        return Result.success(mapper.toOutput(
                commandService.create(mapper.toAuthenticatedSession(request))));
    }

    @Override
    public Result<ExchangeH5SessionHandoffApiCommandOutput> exchangeH5SessionHandoff(
            ExchangeH5SessionHandoffApiCommand request
    ) {
        return Result.success(mapper.toOutput(commandService.exchange(request.ticket())));
    }
}
