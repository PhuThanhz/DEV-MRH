// vn.system.app.modules.careerpath.domain.request.CareerPathBulkRequest

package vn.system.app.modules.careerpath.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathBulkRequest {

    @NotNull(message = "departmentId không được để trống")
    private Long departmentId;

    /**
     * Danh sách jobTitle cần tạo.
     * Tất cả sẽ dùng chung nội dung bên dưới.
     */
    @NotEmpty(message = "Cần ít nhất 1 jobTitleId")
    private List<Long> jobTitleIds;

    // ---- Nội dung dùng chung cho tất cả jobTitle ----
    private String jobStandard;
    private String trainingRequirement;
    private String evaluationMethod;
    private String requiredTime;
    private String trainingOutcome;
    private String performanceRequirement;
    private String salaryNote;
    private Integer status;
}