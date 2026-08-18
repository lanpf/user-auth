package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.enums.SessionHandoffTargetApiEnum;
import com.cloud.userauth.application.session.handoff.CreateSessionHandoffCommand;
import com.cloud.userauth.application.session.handoff.CreateSessionHandoffOutput;
import com.cloud.userauth.application.session.handoff.ExchangeSessionHandoffCommand;
import com.cloud.userauth.application.session.handoff.ExchangeSessionHandoffOutput;
import com.cloud.userauth.application.session.handoff.SessionHandoffTarget;

public interface SessionHandoffApiMapper {
    CreateSessionHandoffCommand toCommand(CreateSessionHandoffApiCommand command);

    ExchangeSessionHandoffCommand toCommand(ExchangeSessionHandoffApiCommand command);

    CreateSessionHandoffApiCommandOutput toOutput(CreateSessionHandoffOutput output);

    ExchangeSessionHandoffApiCommandOutput toOutput(ExchangeSessionHandoffOutput output);

    SessionHandoffTarget toTarget(SessionHandoffTargetApiEnum target);
}
