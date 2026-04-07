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

    private UserInfo user;
    private TemplateInfo template;
    private List<StepProgress> allSteps;
    private StepInfo currentStep;
    private StepInfo nextStep;

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
        private String employeeCode; // ← THÊM
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

    @Getter
    @Setter
    public static class StepProgress {
        private Integer stepOrder;
        private Long careerPathId;
        private String jobTitleName;
        private String positionLevelCode;
        private Integer durationMonths;
        private String stepStatus;
        private LocalDate promotedAt;
        private Long actualMonths;
    }
}