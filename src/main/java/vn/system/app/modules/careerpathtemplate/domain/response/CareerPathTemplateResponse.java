package vn.system.app.modules.careerpathtemplate.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private boolean active;

    // Phòng ban của template
    private Long departmentId;
    private String departmentName;

    private List<StepResponse> steps;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ── Inner DTO ──────────────────────────────────────
    @Getter
    @Setter
    public static class StepResponse {

        private Long id;
        private Integer stepOrder;
        private Integer durationMonths; // null = bước đỉnh (cuối cùng)

        // Thông tin chức danh
        private Long careerPathId;
        private String jobTitleName;
        private String positionLevelCode; // S6, S5, S4...

        private Integer levelNumber;

        // Phòng ban (của chức danh trong bước này)
        private Long departmentId;
        private String departmentName;

        private String description;
    }
}