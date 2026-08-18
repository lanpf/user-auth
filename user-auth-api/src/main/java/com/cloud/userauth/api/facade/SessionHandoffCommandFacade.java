package com.cloud.userauth.api.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import jakarta.validation.Valid;

public interface SessionHandoffCommandFacade {
    Result<CreateSessionHandoffApiCommandOutput> create(@Valid CreateSessionHandoffApiCommand command);

    Result<ExchangeSessionHandoffApiCommandOutput> exchange(@Valid ExchangeSessionHandoffApiCommand command);
}
