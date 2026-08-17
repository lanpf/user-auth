package com.cloud.userauth.interfaces.mapper;

import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.application.authentication.AuthenticatedSession;
import com.cloud.userauth.interfaces.rest.WebViewHandoffController;

public interface SessionHandoffRestMapper {
    CreateH5SessionHandoffApiCommand toCreateCommand(AuthenticatedSession authenticatedSession);

    ExchangeH5SessionHandoffApiCommand toCommand(WebViewHandoffController.ExchangeRequest request);

    WebViewHandoffController.ExchangeRepresentation toRepresentation(
            ExchangeH5SessionHandoffApiCommandOutput output);
}
