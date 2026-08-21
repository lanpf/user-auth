package com.cloud.userauth.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.cloud.framework.core.RequestHeader;
import com.cloud.framework.core.Result;
import com.cloud.framework.starter.webmvc.client.ClientRequestArgumentResolver;
import com.cloud.framework.starter.webmvc.client.ClientRequestBodyAdvice;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeSessionHandoffApiCommandOutput;
import com.cloud.userauth.api.enums.SessionHandoffTargetApiEnum;
import com.cloud.userauth.api.facade.SessionHandoffCommandFacade;
import com.cloud.userauth.interfaces.mapper.mapstruct.SessionHandoffRestMapStructMapper;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HandoffControllerTest {
    @Test
    void shouldCreateBrowserSessionHandoffFromAuthenticatedSession() throws Exception {
        AtomicReference<CreateSessionHandoffApiCommand> captured = new AtomicReference<>();
        SessionHandoffCommandFacade facade = new StubSessionHandoffCommandFacade(captured);
        MockMvc mockMvc = authenticatedMockMvc(facade);
        MvcResult result = mockMvc.perform(post(UserAuthRestPaths.API_HANDOFFS)
                .header(RequestHeader.CLIENT_APP_ID, "mini-program")
                .header(RequestHeader.USER_ID, "1001")
                .header(RequestHeader.SESSION_ID, "session-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"target\":\"BROWSER_SESSION\"}"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(1001L, captured.get().userId());
        assertEquals("session-1", captured.get().sessionId());
        assertEquals(SessionHandoffTargetApiEnum.BROWSER_SESSION, captured.get().target());
        assertTrue(result.getResponse().getContentAsString().contains("ticket-1"));
    }

    @Test
    void shouldDeliverExchangedBrowserSessionOnlyThroughSecureCookie() throws Exception {
        SessionHandoffCommandFacade facade = new StubSessionHandoffCommandFacade(new AtomicReference<>());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HandoffController(
                        facade, Mappers.getMapper(SessionHandoffRestMapStructMapper.class)))
                .setControllerAdvice(new ClientRequestBodyAdvice())
                .build();

        MvcResult result = mockMvc.perform(post(UserAuthRestPaths.API_HANDOFFS_EXCHANGE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "h5-app")
                        .content("{\"ticket\":\"ticket-1\",\"target\":\"BROWSER_SESSION\"}"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        String cookie = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(cookie);
        assertTrue(cookie.contains("BROWSER_SESSION=h5-secret"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertFalse(result.getResponse().getContentAsString().contains("h5-secret"));
    }

    private static MockMvc authenticatedMockMvc(SessionHandoffCommandFacade facade) {
        return MockMvcBuilders.standaloneSetup(new HandoffController(
                        facade, Mappers.getMapper(SessionHandoffRestMapStructMapper.class)))
                .setCustomArgumentResolvers(new ClientRequestArgumentResolver())
                .setControllerAdvice(new ClientRequestBodyAdvice())
                .build();
    }

    private static final class StubSessionHandoffCommandFacade
            implements SessionHandoffCommandFacade {
        private final AtomicReference<CreateSessionHandoffApiCommand> captured;

        private StubSessionHandoffCommandFacade(
                AtomicReference<CreateSessionHandoffApiCommand> captured
        ) {
            this.captured = captured;
        }

        @Override
        public Result<CreateSessionHandoffApiCommandOutput> create(
                CreateSessionHandoffApiCommand request
        ) {
            captured.set(request);
            return Result.success(new CreateSessionHandoffApiCommandOutput(
                    "handoff-1", "ticket-1"));
        }

        @Override
        public Result<ExchangeSessionHandoffApiCommandOutput> exchange(
                ExchangeSessionHandoffApiCommand request
        ) {
            return Result.success(new ExchangeSessionHandoffApiCommandOutput(
                    "h5-secret", 1800L, "handoff-1"));
        }
    }
}
