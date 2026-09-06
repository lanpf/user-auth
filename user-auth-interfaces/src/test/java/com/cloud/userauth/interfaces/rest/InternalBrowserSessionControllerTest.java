package com.cloud.userauth.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.cloud.framework.core.Result;
import com.cloud.userauth.api.authentication.EndBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommand;
import com.cloud.userauth.api.authentication.VerifyBrowserSessionApiCommandOutput;
import com.cloud.userauth.api.constants.UserAuthPathApiConstants;
import com.cloud.userauth.api.facade.BrowserSessionCommandFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InternalBrowserSessionControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldReportVerifiedSessionWithSubjectIdentity() throws Exception {
        VerifyBrowserSessionApiCommandOutput output =
                VerifyBrowserSessionApiCommandOutput.verified(1001L, "session-1", 1800L);
        AtomicReference<VerifyBrowserSessionApiCommand> captured = new AtomicReference<>();
        MockMvc mockMvc = mockMvc(output, captured);

        String body = mockMvc.perform(post(UserAuthPathApiConstants.INTERNAL_BROWSER_SESSIONS_VERIFY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"credential\":\"browser-token\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(new VerifyBrowserSessionApiCommand("browser-token"), captured.get());
        assertEquals(OBJECT_MAPPER.writeValueAsString(Result.success(output)),
                normalize(body));
    }

    @Test
    void shouldReportUnverifiedForUnknownCredential() throws Exception {
        VerifyBrowserSessionApiCommandOutput output =
                VerifyBrowserSessionApiCommandOutput.unverified();
        MockMvc mockMvc = mockMvc(output, new AtomicReference<>());

        String body = mockMvc.perform(post(UserAuthPathApiConstants.INTERNAL_BROWSER_SESSIONS_VERIFY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"credential\":\"missing\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(OBJECT_MAPPER.writeValueAsString(Result.success(output)),
                normalize(body));
    }

    private static MockMvc mockMvc(
            VerifyBrowserSessionApiCommandOutput output,
            AtomicReference<VerifyBrowserSessionApiCommand> captured
    ) {
        BrowserSessionCommandFacade facade = new BrowserSessionCommandFacade() {
            @Override
            public Result<VerifyBrowserSessionApiCommandOutput> verify(
                    VerifyBrowserSessionApiCommand command
            ) {
                captured.set(command);
                return Result.success(output);
            }

            @Override
            public Result<Void> end(EndBrowserSessionApiCommand command) {
                throw new UnsupportedOperationException();
            }
        };
        return MockMvcBuilders.standaloneSetup(
                new InternalBrowserSessionController(facade)).build();
    }

    private static String normalize(String body) {
        return body.replaceAll("\\s+", "");
    }
}
