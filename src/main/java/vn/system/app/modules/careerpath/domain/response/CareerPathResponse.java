// vn.system.app.modules.careerpath.domain.response.CareerPathResponse

package vn.system.app.modules.careerpath.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathResponse {

    private Long id;

    private Long departmentId;
    private String departmentName;

    private Long jobTitleId;
    private String jobTitleName;
    private String positionLevelCode;
    private Integer bandOrder;
    private Integer levelNumber; // thêm để dễ sort

    private String jobStandard;
    private String trainingRequirement;
    private String evaluationMethod;
    private String requiredTime;
    private String trainingOutcome;
    private String performanceRequirement;
    private String salaryNote;

    private Integer status;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}