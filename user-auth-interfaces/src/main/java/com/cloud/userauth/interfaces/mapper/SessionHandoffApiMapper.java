package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.session.handoff.CreateH5SessionHandoffCommandOutput;
import com.cloud.userauth.application.session.handoff.ExchangeH5SessionHandoffCommandOutput;

public interface SessionHandoffApiMapper {
    AuthenticatedSession toAuthenticatedSession(CreateH5SessionHandoffApiCommand command);

    CreateH5SessionHandoffApiCommandOutput toOutput(CreateH5SessionHandoffCommandOutput output);

    ExchangeH5SessionHandoffApiCommandOutput toOutput(ExchangeH5SessionHandoffCommandOutput output);
}
