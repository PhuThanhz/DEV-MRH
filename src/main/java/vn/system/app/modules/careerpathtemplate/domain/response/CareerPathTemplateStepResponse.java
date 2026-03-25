package vn.system.app.modules.careerpathtemplate.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathTemplateStepResponse {

    private Long id;
    private Integer stepOrder;
    private Integer durationMonths; // null = bước đỉnh (cuối cùng)

    // Thông tin chức danh
    private Long careerPathId;
    private String jobTitleName;
    private String positionLevelCode; // S6, S5, S4...
    private Integer levelNumber; // số thứ tự trong band

    // Phòng ban
    private Long departmentId;
    private String departmentName;

    private String description;
}