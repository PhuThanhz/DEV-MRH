package vn.system.app.modules.companyjobtitle.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCompanyJobTitleDTO {

    @NotNull(message = "CompanyId không được để trống")
    private Long companyId;

    @NotNull(message = "JobTitleId không được để trống")
    private Long jobTitleId;
}
