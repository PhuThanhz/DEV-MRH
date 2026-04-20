package vn.system.app.modules.jd.jdflow.domain.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ResJdApproverDTO {

    private String id;
    private String name;
    private String email;
    private String avatar;
    private boolean isFinal;

    // ✅ THÊM — danh sách chức danh của user
    private List<PositionInfo> positions;

    public ResJdApproverDTO() {
    }

    // Constructor cũ — giữ lại để không break code
    public ResJdApproverDTO(String id, String name, String email, String avatar, boolean isFinal) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.isFinal = isFinal;
    }

    @Getter
    @Setter
    public static class PositionInfo {
        private String companyName;
        private String departmentName; // null nếu source = COMPANY
        private String jobTitleName;
        private String positionCode; // ví dụ: M3, E2...
        private String source; // COMPANY / DEPARTMENT / SECTION
    }
}