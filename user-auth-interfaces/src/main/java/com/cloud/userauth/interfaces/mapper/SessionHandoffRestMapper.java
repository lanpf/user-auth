package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import com.cloud.userauth.interfaces.rest.HandoffController;

public interface SessionHandoffRestMapper {
    CreateSessionHandoffApiCommand toCommand(HandoffController.CreateRequest request);

    ExchangeSessionHandoffApiCommand toCommand(HandoffController.ExchangeRequest request);

    HandoffController.ExchangeRepresentation toRepresentation(
            ExchangeSessionHandoffApiCommandOutput output);
}
