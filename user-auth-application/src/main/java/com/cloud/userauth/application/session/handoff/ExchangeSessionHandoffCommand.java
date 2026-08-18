package com.cloud.userauth.application.session.handoff;

public record ExchangeSessionHandoffCommand(String ticket, SessionHandoffTarget target) {
}
