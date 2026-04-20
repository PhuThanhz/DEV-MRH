package vn.system.app.modules.departmentprocedure.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentProcedureDTO {

    private Long id;

    // ✅ Đổi từ 1 department sang list
    private List<DepartmentRef> departments;

    private Long sectionId;
    private String sectionName;

    private String procedureCode;
    private String procedureName;
    private String status;
    private Integer planYear;
    private Instant issuedDate;
    private List<String> fileUrls;
    private String note;
    private boolean active;
    private Integer version;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ✅ Inner class chứa thông tin phòng ban
    @Getter
    @Setter
    public static class DepartmentRef {
        private Long id;
        private String name;
        private Long companyId; // ✅ THÊM

        private String companyName;
        private String companyCode;
    }
}