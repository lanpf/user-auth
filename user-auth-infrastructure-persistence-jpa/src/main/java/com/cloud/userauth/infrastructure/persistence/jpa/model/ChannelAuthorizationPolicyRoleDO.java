package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(ChannelAuthorizationPolicyRoleDO.Key.class)
public class ChannelAuthorizationPolicyRoleDO {
    @Id
    private String channelCode;
    @Id
    private String roleCode;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Key implements Serializable {
        private String channelCode;
        private String roleCode;

        public Key(String channelCode, String roleCode) {
            this.channelCode = channelCode;
            this.roleCode = roleCode;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key
                    && Objects.equals(channelCode, key.channelCode)
                    && Objects.equals(roleCode, key.roleCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelCode, roleCode);
        }
    }
}
