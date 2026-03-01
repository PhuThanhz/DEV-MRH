package vn.system.app.modules.position.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqPositionDTO {

    @NotNull(message = "Company ID không được để trống")
    private Long companyId;

    private Long departmentId;

    private Long sectionId;

    @NotNull(message = "JobTitle ID không được để trống")
    private Long jobTitleId;

    private Long reportsToId;

    // ⭐ USER SẼ NGỒI VÀO GHẾ NÀY
    private Long employeeId;
}