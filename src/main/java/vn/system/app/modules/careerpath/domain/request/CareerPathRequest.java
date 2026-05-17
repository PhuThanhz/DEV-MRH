package vn.system.app.modules.careerpath.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathRequest {

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;

    @NotNull(message = "Chức danh không được để trống")
    private Long jobTitleId;

    private String jobStandard;
    private String trainingRequirement;
    private String evaluationMethod;
    private String requiredTime;
    private String trainingOutcome;
    private String performanceRequirement;
    private String salaryNote;

    private Integer status;
}