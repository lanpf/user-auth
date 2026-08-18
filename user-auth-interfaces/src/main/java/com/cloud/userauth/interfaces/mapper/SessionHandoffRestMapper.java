package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import com.cloud.userauth.interfaces.rest.WebViewHandoffController;

public interface SessionHandoffRestMapper {
    CreateSessionHandoffApiCommand toCommand(WebViewHandoffController.CreateRequest request);

    ExchangeSessionHandoffApiCommand toCommand(WebViewHandoffController.ExchangeRequest request);

    WebViewHandoffController.ExchangeRepresentation toRepresentation(
            ExchangeSessionHandoffApiCommandOutput output);
}
