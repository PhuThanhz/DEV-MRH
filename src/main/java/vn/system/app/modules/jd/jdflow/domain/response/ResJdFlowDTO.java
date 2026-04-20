package vn.system.app.modules.jd.jdflow.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResJdFlowDTO {

    private Long jdId;

    private String code;

    private String status;

    private UserInfo fromUser;

    private UserInfo currentUser;

    private Instant updatedAt;

    private boolean currentUserIsFinal; // ✅ thêm field này

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {

        private String id;

        private String name;

    }
}