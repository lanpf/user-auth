package com.cloud.userauth.interfaces.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.interfaces.mapper.SessionHandoffRestMapper;
import com.cloud.userauth.interfaces.rest.HandoffController;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface SessionHandoffRestMapStructMapper extends SessionHandoffRestMapper {
    @Override
    CreateSessionHandoffApiCommand toCommand(HandoffController.CreateRequest request);
}
