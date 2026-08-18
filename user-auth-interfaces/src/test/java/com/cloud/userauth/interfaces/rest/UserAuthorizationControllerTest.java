package com.cloud.userauth.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.cloud.framework.core.RequestHeader;
import com.cloud.framework.core.Result;
import com.cloud.framework.core.PageResult;
import com.cloud.framework.starter.webmvc.client.ClientRequestBodyAdvice;
import com.cloud.userauth.api.authorization.ChangeChannelAuthorizationPolicyStatusApiCommand;
import com.cloud.userauth.api.authorization.ChangePermissionStatusApiCommand;
import com.cloud.userauth.api.authorization.ChangeRoleStatusApiCommand;
import com.cloud.userauth.api.authorization.AuthorizationCatalogApiQuery;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiQuery;
import com.cloud.userauth.api.authorization.ChannelAuthorizationPolicyApiResponse;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.ReconcileChannelAuthorizationPolicyApiCommandOutput;
import com.cloud.userauth.api.authorization.PermissionApiQuery;
import com.cloud.userauth.api.authorization.PermissionApiResponse;
import com.cloud.userauth.api.authorization.RoleApiQuery;
import com.cloud.userauth.api.authorization.RoleApiResponse;
import com.cloud.userauth.api.authorization.SaveChannelAuthorizationPolicyApiCommand;
import com.cloud.userauth.api.authorization.SavePermissionApiCommand;
import com.cloud.userauth.api.authorization.SaveRoleApiCommand;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQuery;
import com.cloud.userauth.api.authorization.UserAuthorizationApiQueryView;
import com.cloud.userauth.api.facade.UserAuthorizationCommandFacade;
import com.cloud.userauth.api.facade.UserAuthorizationQueryFacade;
import com.cloud.userauth.interfaces.mapper.mapstruct.AuthorizationRestMapStructMapper;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserAuthorizationControllerTest {

    @Test
    void shouldUseExplicitTargetChannelInsteadOfChannelHeader() throws Exception {
        AtomicReference<ChannelAuthorizationPolicyApiQuery> captured = new AtomicReference<>();
        MockMvc mockMvc = mockMvc(new StubQueryFacade(captured, new AtomicReference<>()));

        int status = mockMvc.perform(post(
                        UserAuthRestPaths.ADMIN_AUTHORIZATION_CHANNEL_POLICIES_QUERY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "admin-console")
                        .header(RequestHeader.CHANNEL_CODE, "HEADER_CHANNEL")
                        .content("{\"targetChannelCode\":\"TARGET_CHANNEL\"}"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertEquals(200, status);
        assertEquals("TARGET_CHANNEL", captured.get().channelCode());
    }

    @Test
    void shouldUseExplicitTargetUserInsteadOfUserHeader() throws Exception {
        AtomicReference<UserAuthorizationApiQuery> captured = new AtomicReference<>();
        MockMvc mockMvc = mockMvc(new StubQueryFacade(new AtomicReference<>(), captured));

        int status = mockMvc.perform(post(
                        UserAuthRestPaths.ADMIN_AUTHORIZATION_USERS_QUERY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "admin-console")
                        .header(RequestHeader.USER_ID, "1001")
                        .content("{\"targetUserId\":2002}"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertEquals(200, status);
        assertEquals(2002L, captured.get().userId());
    }

    @Test
    void shouldRejectCatalogPageSizeAboveSharedLimit() throws Exception {
        MockMvc mockMvc = mockMvc(new StubQueryFacade(new AtomicReference<>(), new AtomicReference<>()));

        int status = mockMvc.perform(post(
                        UserAuthRestPaths.ADMIN_AUTHORIZATION_PERMISSIONS_QUERY_PAGE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(RequestHeader.CLIENT_APP_ID, "admin-console")
                        .content("{\"pageNo\":1,\"pageSize\":201}"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertEquals(400, status);
    }

    private static MockMvc mockMvc(UserAuthorizationQueryFacade queryFacade) {
        return MockMvcBuilders.standaloneSetup(
                        new UserAuthorizationController(
                                new StubCommandFacade(), queryFacade,
                                Mappers.getMapper(AuthorizationRestMapStructMapper.class)))
                .setControllerAdvice(new ClientRequestBodyAdvice())
                .build();
    }

    private static final class StubQueryFacade implements UserAuthorizationQueryFacade {
        private final AtomicReference<ChannelAuthorizationPolicyApiQuery> channelQuery;
        private final AtomicReference<UserAuthorizationApiQuery> userQuery;

        private StubQueryFacade(
                AtomicReference<ChannelAuthorizationPolicyApiQuery> channelQuery,
                AtomicReference<UserAuthorizationApiQuery> userQuery
        ) {
            this.channelQuery = channelQuery;
            this.userQuery = userQuery;
        }

        @Override
        public Result<ChannelAuthorizationPolicyApiResponse> getChannelPolicy(
                ChannelAuthorizationPolicyApiQuery request
        ) {
            channelQuery.set(request);
            return Result.success(new ChannelAuthorizationPolicyApiResponse(
                    request.channelCode(), "ACTIVE", 1L, List.of(), List.of(),
                    Instant.EPOCH, Instant.EPOCH, Instant.EPOCH));
        }

        @Override
        public Result<UserAuthorizationApiQueryView> getUserAuthorization(
                UserAuthorizationApiQuery request
        ) {
            userQuery.set(request);
            return Result.success(new UserAuthorizationApiQueryView(
                    request.userId(), List.of(), List.of(), List.of(), List.of()));
        }

        @Override public Result<PermissionApiResponse> getPermission(PermissionApiQuery request) {
            throw new UnsupportedOperationException();
        }
        @Override public PageResult<PermissionApiResponse> getPermissions(AuthorizationCatalogApiQuery request) {
            throw new UnsupportedOperationException();
        }
        @Override public Result<RoleApiResponse> getRole(RoleApiQuery request) {
            throw new UnsupportedOperationException();
        }
        @Override public PageResult<RoleApiResponse> getRoles(AuthorizationCatalogApiQuery request) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubCommandFacade implements UserAuthorizationCommandFacade {
        @Override public Result<PermissionApiResponse> savePermission(SavePermissionApiCommand request) {
            throw new UnsupportedOperationException();
        }
        @Override public Result<PermissionApiResponse> activatePermission(ChangePermissionStatusApiCommand request) {
            throw new UnsupportedOperationException();
        }
        @Override public Result<PermissionApiResponse> disablePermission(ChangePermissionStatusApiCommand request) {
            throw new UnsupportedOperationException();
        }
        @Override public Result<RoleApiResponse> saveRole(SaveRoleApiCommand request) {
            throw new UnsupportedOperationException();
        }
        @Override public Result<RoleApiResponse> activateRole(ChangeRoleStatusApiCommand request) {
            throw new UnsupportedOperationException();
        }
        @Override public Result<RoleApiResponse> disableRole(ChangeRoleStatusApiCommand request) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Result<ChannelAuthorizationPolicyApiResponse> saveChannelPolicy(
                SaveChannelAuthorizationPolicyApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<ChannelAuthorizationPolicyApiResponse> activateChannelPolicy(
                ChangeChannelAuthorizationPolicyStatusApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<ChannelAuthorizationPolicyApiResponse> disableChannelPolicy(
                ChangeChannelAuthorizationPolicyStatusApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result<ReconcileChannelAuthorizationPolicyApiCommandOutput> reconcileChannelPolicy(
                ReconcileChannelAuthorizationPolicyApiCommand request
        ) {
            throw new UnsupportedOperationException();
        }
    }
}
