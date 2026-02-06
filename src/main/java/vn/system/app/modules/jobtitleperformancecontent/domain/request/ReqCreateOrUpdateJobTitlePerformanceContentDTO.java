package vn.system.app.modules.jobtitleperformancecontent.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.jobtitleperformancecontent.domain.OwnerLevel;

@Getter
@Setter
public class ReqCreateOrUpdateJobTitlePerformanceContentDTO {

    @NotNull
    private OwnerLevel ownerLevel;

    @NotNull
    private Long ownerJobTitleId;

    @NotNull
    private Long salaryGradeId;

    private String contentA;
    private String contentB;
    private String contentC;
    private String contentD;
}
