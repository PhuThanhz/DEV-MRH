package vn.system.app.modules.employeecareerpath.domain.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResEmployeeCareerPathDTO {

    private Long id;

    // Thông tin nhân viên
    private UserInfo user;

    // Thông tin template lộ trình
    private TemplateInfo template;

    // Toàn bộ lộ trình — nhìn thấy hết từ đầu đến cuối
    private List<StepProgress> allSteps;

    // Bước hiện tại & tiếp theo (shortcut)
    private StepInfo currentStep;
    private StepInfo nextStep;

    // Tiến độ
    private Integer currentStepOrder;
    private Integer totalSteps;
    private LocalDate stepStartedAt;
    private Long daysInCurrentStep;
    private Integer durationMonths;
    private boolean overdue;

    private Integer progressStatus;
    private String progressStatusLabel;

    private String note;
    private boolean active;

    private List<ResEmployeeCareerPathHistoryDTO> histories;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ── Inner DTOs ─────────────────────────────────────

    @Getter
    @Setter
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
    }

    @Getter
    @Setter
    public static class TemplateInfo {
        private Long id;
        private String name;
        private Long departmentId;
        private String departmentName;
    }

    @Getter
    @Setter
    public static class StepInfo {
        private Integer stepOrder;
        private Long careerPathId;
        private String jobTitleName;
        private String positionLevelCode;
        private Integer durationMonths;
    }

    // Dùng cho allSteps — mỗi bước có thêm trạng thái + ngày thăng tiến thực tế
    @Getter
    @Setter
    public static class StepProgress {
        private Integer stepOrder;
        private Long careerPathId;
        private String jobTitleName;
        private String positionLevelCode;
        private Integer durationMonths;

        /*
         * stepStatus:
         * COMPLETED — đã qua bước này (có ngày thăng tiến)
         * CURRENT — đang ở bước này
         * UPCOMING — chưa đến
         */
        private String stepStatus;

        // Ngày thăng tiến thực tế (chỉ có khi COMPLETED)
        private LocalDate promotedAt;

        // Số tháng thực tế đã ở bước này (chỉ có khi COMPLETED)
        private Long actualMonths;
    }
}