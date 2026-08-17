package com.cloud.userauth.api.facade;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommandOutput;
import jakarta.validation.Valid;

public interface SessionHandoffCommandFacade {
    Result<CreateH5SessionHandoffApiCommandOutput> createH5SessionHandoff(
            @Valid CreateH5SessionHandoffApiCommand command);

    Result<ExchangeH5SessionHandoffApiCommandOutput> exchangeH5SessionHandoff(
            @Valid ExchangeH5SessionHandoffApiCommand command);
}
