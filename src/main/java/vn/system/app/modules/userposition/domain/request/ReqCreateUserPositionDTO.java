package vn.system.app.modules.userposition.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateUserPositionDTO {

    @NotBlank(message = "Source không được để trống")
    private String source; // COMPANY / DEPARTMENT / SECTION

    // Chỉ 1 trong 3 có giá trị tùy source
    private Long companyJobTitleId;
    private Long departmentJobTitleId;
    private Long sectionJobTitleId;
}