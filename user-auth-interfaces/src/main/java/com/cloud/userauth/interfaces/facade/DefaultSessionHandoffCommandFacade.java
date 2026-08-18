package com.cloud.userauth.interfaces.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.facade.SessionHandoffCommandFacade;
import com.cloud.userauth.application.session.handoff.SessionHandoffCommandService;
import com.cloud.userauth.interfaces.mapper.SessionHandoffApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class DefaultSessionHandoffCommandFacade implements SessionHandoffCommandFacade {
    private final SessionHandoffCommandService commandService;
    private final SessionHandoffApiMapper mapper;

    @Override
    public Result<CreateSessionHandoffApiCommandOutput> create(
            CreateSessionHandoffApiCommand command
    ) {
        return Result.success(mapper.toOutput(commandService.create(mapper.toCommand(command))));
    }

    @Override
    public Result<ExchangeSessionHandoffApiCommandOutput> exchange(
            ExchangeSessionHandoffApiCommand command
    ) {
        return Result.success(mapper.toOutput(commandService.exchange(mapper.toCommand(command))));
    }
}
