package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import com.cloud.userauth.application.session.handoff.CreateSessionHandoffCommand;
import com.cloud.userauth.application.session.handoff.CreateSessionHandoffOutput;
import com.cloud.userauth.application.session.handoff.ExchangeSessionHandoffCommand;
import com.cloud.userauth.application.session.handoff.ExchangeSessionHandoffOutput;
import com.cloud.userauth.interfaces.mapper.SessionHandoffApiMapper;
import java.time.Duration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface SessionHandoffApiMapStructMapper extends SessionHandoffApiMapper {
    @Override
    @Mapping(target = "authenticatedUserId", source = "userId")
    @Mapping(target = "authenticatedSessionId", source = "sessionId")
    CreateSessionHandoffCommand toCommand(CreateSessionHandoffApiCommand command);

    @Override
    ExchangeSessionHandoffCommand toCommand(ExchangeSessionHandoffApiCommand command);

    @Override
    CreateSessionHandoffApiCommandOutput toOutput(CreateSessionHandoffOutput output);

    @Override
    @Mapping(target = "expiresIn", source = "sessionTtl")
    ExchangeSessionHandoffApiCommandOutput toOutput(ExchangeSessionHandoffOutput output);

    default long toSeconds(Duration duration) {
        return duration.toSeconds();
    }
}
