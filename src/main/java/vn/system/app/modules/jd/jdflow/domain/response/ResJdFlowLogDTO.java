package vn.system.app.modules.jd.jdflow.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJdFlowLogDTO {

    private Long id;

    private Long jdId;

    private UserInfo fromUser;

    private UserInfo toUser;

    private String action;

    private String comment;

    private Instant createdAt;

    @Getter
    @Setter
    public static class UserInfo {

        private Long id;
        private String name;

        public UserInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}