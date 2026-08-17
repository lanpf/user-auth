package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommand;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.interfaces.mapper.SessionHandoffRestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface SessionHandoffRestMapStructMapper extends SessionHandoffRestMapper {
    @Override
    @Mapping(target = "sessionId", source = "sessionId.value")
    CreateH5SessionHandoffApiCommand toCreateCommand(AuthenticatedSession authenticatedSession);
}
