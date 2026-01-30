package vn.system.app.modules.sectionjobtitle.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSectionJobTitleDTO {

    @NotNull(message = "JobTitleId không được để trống")
    private Long jobTitleId;

    @NotNull(message = "SectionId không được để trống")
    private Long sectionId;
}
