package vn.system.app.modules.orgjobtitle.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateOrgJobTitleDTO {

    @NotNull(message = "JobTitleId không được để trống")
    private Long jobTitleId; // id chức danh

    // ===== CHỈ 1 TRONG 3 =====

    private Long companyId; // id công ty

    private Long departmentId; // id phòng ban

    private Long sectionId; // id bộ phận
}
