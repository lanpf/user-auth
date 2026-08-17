package com.cloud.userauth.infrastructure.persistence.jpa.mapper.mapstruct;

import com.cloud.framework.core.mapper.MapStructConfig;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicy;
import com.cloud.userauth.domain.authorization.ChannelAuthorizationPolicyStatus;
import com.cloud.userauth.domain.authorization.ChannelCode;
import com.cloud.userauth.domain.authorization.GrantId;
import com.cloud.userauth.domain.authorization.GrantSource;
import com.cloud.userauth.domain.authorization.GrantSourceType;
import com.cloud.userauth.domain.authorization.GrantStatus;
import com.cloud.userauth.domain.authorization.Permission;
import com.cloud.userauth.domain.authorization.PermissionCode;
import com.cloud.userauth.domain.authorization.PermissionStatus;
import com.cloud.userauth.domain.authorization.Role;
import com.cloud.userauth.domain.authorization.RoleCode;
import com.cloud.userauth.domain.authorization.RoleStatus;
import com.cloud.userauth.domain.authorization.UserChannelPolicyApplication;
import com.cloud.userauth.domain.authorization.UserPermissionGrant;
import com.cloud.userauth.domain.authorization.UserRoleGrant;
import com.cloud.userauth.domain.user.UserId;
import com.cloud.userauth.infrastructure.persistence.jpa.mapper.AuthorizationPersistenceMapper;
import com.cloud.userauth.infrastructure.persistence.jpa.model.ChannelAuthorizationPolicyDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.PermissionDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.RoleDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.UserChannelPolicyApplicationDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.UserPermissionGrantDO;
import com.cloud.userauth.infrastructure.persistence.jpa.model.UserRoleGrantDO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AuthorizationPersistenceMapStructMapper extends AuthorizationPersistenceMapper {
    @Override
    @Mapping(target = "roleCode", source = "roleCode.value")
    @Mapping(target = "status", source = "status")
    RoleDO toDataObject(Role source);

    @Override
    default Role toDomain(RoleDO source) {
        return Role.restore(
                new RoleCode(source.getRoleCode()),
                source.getRoleName(),
                RoleStatus.valueOf(source.getStatus()),
                List.of(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    @Override
    @Mapping(target = "permissionCode", source = "permissionCode.value")
    @Mapping(target = "status", source = "status")
    PermissionDO toDataObject(Permission source);

    @Override
    default Permission toDomain(PermissionDO source) {
        return Permission.restore(
                new PermissionCode(source.getPermissionCode()),
                source.getPermissionName(),
                source.getOwnerService(),
                PermissionStatus.valueOf(source.getStatus()),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "roleCode", source = "roleCode.value")
    @Mapping(target = "sourceType", source = "source.sourceType")
    @Mapping(target = "sourceId", source = "source.sourceId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "grantedBy", source = "grantedBy.value")
    UserRoleGrantDO toDataObject(UserRoleGrant source);

    @Override
    default UserRoleGrant toDomain(UserRoleGrantDO source) {
        return UserRoleGrant.restore(
                new GrantId(source.getId()),
                new UserId(source.getUserId()),
                new RoleCode(source.getRoleCode()),
                new GrantSource(
                        GrantSourceType.valueOf(source.getSourceType()),
                        source.getSourceId()),
                GrantStatus.valueOf(source.getStatus()),
                userId(source.getGrantedBy()),
                source.getGrantedAt(),
                source.getExpiresAt(),
                source.getUpdatedAt());
    }

    @Override
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "permissionCode", source = "permissionCode.value")
    @Mapping(target = "sourceType", source = "source.sourceType")
    @Mapping(target = "sourceId", source = "source.sourceId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "grantedBy", source = "grantedBy.value")
    UserPermissionGrantDO toDataObject(UserPermissionGrant source);

    @Override
    default UserPermissionGrant toDomain(UserPermissionGrantDO source) {
        return UserPermissionGrant.restore(
                new GrantId(source.getId()),
                new UserId(source.getUserId()),
                new PermissionCode(source.getPermissionCode()),
                new GrantSource(
                        GrantSourceType.valueOf(source.getSourceType()),
                        source.getSourceId()),
                GrantStatus.valueOf(source.getStatus()),
                userId(source.getGrantedBy()),
                source.getGrantedAt(),
                source.getExpiresAt(),
                source.getReason(),
                source.getUpdatedAt());
    }

    @Override
    @Mapping(target = "channelCode", source = "channelCode.value")
    @Mapping(target = "status", source = "status")
    ChannelAuthorizationPolicyDO toDataObject(ChannelAuthorizationPolicy source);

    @Override
    default ChannelAuthorizationPolicy toDomain(ChannelAuthorizationPolicyDO source) {
        return ChannelAuthorizationPolicy.restore(
                new ChannelCode(source.getChannelCode()),
                ChannelAuthorizationPolicyStatus.valueOf(source.getStatus()),
                source.getVersion(),
                List.of(),
                List.of(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getActivatedAt());
    }

    @Override
    @Mapping(target = "userId", source = "userId.value")
    @Mapping(target = "channelCode", source = "channelCode.value")
    UserChannelPolicyApplicationDO toDataObject(UserChannelPolicyApplication source);

    @Override
    default UserChannelPolicyApplication toDomain(UserChannelPolicyApplicationDO source) {
        return UserChannelPolicyApplication.restore(
                new UserId(source.getUserId()),
                new ChannelCode(source.getChannelCode()),
                source.getAppliedVersion(),
                source.getFirstAppliedAt(),
                source.getLastAppliedAt());
    }

    default String map(RoleCode value) {
        return value == null ? null : value.value();
    }

    default String map(PermissionCode value) {
        return value == null ? null : value.value();
    }

    private static UserId userId(Long value) {
        return value == null ? null : new UserId(value);
    }
}
