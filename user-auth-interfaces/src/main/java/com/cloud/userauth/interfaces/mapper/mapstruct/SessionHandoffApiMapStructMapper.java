package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.application.session.handoff.CreateH5SessionHandoffCommandOutput;
import com.cloud.userauth.application.session.handoff.ExchangeH5SessionHandoffCommandOutput;
import com.cloud.userauth.domain.authentication.session.SessionId;
import com.cloud.userauth.interfaces.mapper.SessionHandoffApiMapper;
import java.time.Duration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface SessionHandoffApiMapStructMapper extends SessionHandoffApiMapper {
    @Override
    AuthenticatedSession toAuthenticatedSession(CreateH5SessionHandoffApiCommand command);

    @Override
    CreateH5SessionHandoffApiCommandOutput toOutput(CreateH5SessionHandoffCommandOutput output);

    @Override
    @Mapping(target = "expiresIn", source = "sessionTtl")
    ExchangeH5SessionHandoffApiCommandOutput toOutput(ExchangeH5SessionHandoffCommandOutput output);

    default SessionId toSessionId(String sessionId) {
        return new SessionId(sessionId);
    }

    default long toSeconds(Duration duration) {
        return duration.toSeconds();
    }
}
