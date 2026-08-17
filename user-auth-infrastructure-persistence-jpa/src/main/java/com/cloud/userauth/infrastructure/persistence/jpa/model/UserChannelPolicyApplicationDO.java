package com.cloud.userauth.infrastructure.persistence.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(UserChannelPolicyApplicationDO.Key.class)
public class UserChannelPolicyApplicationDO {
    @Id
    private Long userId;
    @Id
    private String channelCode;
    private Long appliedVersion;
    private Instant firstAppliedAt;
    private Instant lastAppliedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Key implements Serializable {
        private Long userId;
        private String channelCode;

        public Key(Long userId, String channelCode) {
            this.userId = userId;
            this.channelCode = channelCode;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key key)) {
                return false;
            }
            return Objects.equals(userId, key.userId)
                    && Objects.equals(channelCode, key.channelCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, channelCode);
        }
    }
}
