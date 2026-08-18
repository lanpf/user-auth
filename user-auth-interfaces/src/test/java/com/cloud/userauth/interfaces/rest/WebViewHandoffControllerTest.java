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
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.CreateH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommand;
import com.cloud.userauth.api.authentication.ExchangeH5SessionHandoffApiCommandOutput;
import com.cloud.userauth.api.constants.AccessTokenClaimApiConstants;
import com.cloud.userauth.api.facade.SessionHandoffCommandFacade;
import com.cloud.userauth.interfaces.mapper.mapstruct.SessionHandoffRestMapStructMapper;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WebViewHandoffControllerTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateH5SessionHandoffFromAuthenticatedSession() throws Exception {
        AtomicReference<CreateH5SessionHandoffApiCommand> captured = new AtomicReference<>();
        SessionHandoffCommandFacade facade = new StubSessionHandoffCommandFacade(captured);
        MockMvc mockMvc = authenticatedMockMvc(facade);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt()));

        MvcResult result = mockMvc.perform(post(UserAuthRestPaths.API_WEB_VIEW_HANDOFFS)
                        .header(RequestHeader.CLIENT_APP_ID, "mini-program"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        assertEquals(1001L, captured.get().userId());
        assertEquals(2001L, captured.get().authAccountId());
        assertEquals("session-1", captured.get().sessionId());
        assertTrue(result.getResponse().getContentAsString().contains("ticket-1"));
    }

    @Test
    void shouldDeliverExchangedH5SessionOnlyThroughSecureCookie() throws Exception {
        SessionHandoffCommandFacade facade = new StubSessionHandoffCommandFacade(new AtomicReference<>());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WebViewHandoffController(
                        facade, Mappers.getMapper(SessionHandoffRestMapStructMapper.class)))
                .setControllerAdvice(new ClientRequestBodyAdvice())
                .build();

        MvcResult result = mockMvc.perform(post(UserAuthRestPaths.API_WEB_VIEW_HANDOFFS_EXCHANGE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "h5-app")
                        .content("{\"ticket\":\"ticket-1\"}"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        String cookie = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(cookie);
        assertTrue(cookie.contains("H5_SESSION=h5-secret"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertFalse(result.getResponse().getContentAsString().contains("h5-secret"));
    }

    private static MockMvc authenticatedMockMvc(SessionHandoffCommandFacade facade) {
        return MockMvcBuilders.standaloneSetup(new WebViewHandoffController(
                        facade, Mappers.getMapper(SessionHandoffRestMapStructMapper.class)))
                .setCustomArgumentResolvers(
                        new ClientRequestArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private static Jwt jwt() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim(AccessTokenClaimApiConstants.USER_ID_CLAIM, 1001L)
                .claim(AccessTokenClaimApiConstants.AUTH_ACCOUNT_ID_CLAIM, 2001L)
                .claim(AccessTokenClaimApiConstants.SESSION_ID_CLAIM, "session-1")
                .build();
    }

    private static final class StubSessionHandoffCommandFacade
            implements SessionHandoffCommandFacade {
        private final AtomicReference<CreateH5SessionHandoffApiCommand> captured;

        private StubSessionHandoffCommandFacade(
                AtomicReference<CreateH5SessionHandoffApiCommand> captured
        ) {
            this.captured = captured;
        }

        @Override
        public Result<CreateH5SessionHandoffApiCommandOutput> createH5SessionHandoff(
                CreateH5SessionHandoffApiCommand request
        ) {
            captured.set(request);
            return Result.success(new CreateH5SessionHandoffApiCommandOutput(
                    "handoff-1", "ticket-1"));
        }

        @Override
        public Result<ExchangeH5SessionHandoffApiCommandOutput> exchangeH5SessionHandoff(
                ExchangeH5SessionHandoffApiCommand request
        ) {
            return Result.success(new ExchangeH5SessionHandoffApiCommandOutput(
                    "h5-secret", 1800L, "handoff-1"));
        }
    }
}
