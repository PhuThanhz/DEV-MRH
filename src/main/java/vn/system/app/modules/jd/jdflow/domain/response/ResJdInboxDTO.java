package vn.system.app.modules.jd.jdflow.domain.response;

import lombok.Data;
import java.time.Instant;

@Data
public class ResJdInboxDTO {

    private Long jdId;
    private String code;
    private String companyName;
    private String departmentName;
    private String jobTitleName;
    private String status;
    private UserSimple fromUser;
    private UserSimple currentUser;
    private Instant updatedAt;

    // ── LÝ DO TỪ CHỐI ──
    private String rejectComment;
    private String rejectorName;
    private String rejectorPosition;
    private String rejectorDepartment;
    private String rejectorPositionCode; // ← THÊM

    @Data
    public static class UserSimple {
        private Long id;
        private String name;
    }
}