// vn.system.app.modules.careerpath.domain.request.CareerPathRequest

package vn.system.app.modules.careerpath.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathRequest {

    private Long departmentId;
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