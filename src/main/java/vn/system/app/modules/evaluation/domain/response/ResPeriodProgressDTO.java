package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class ResPeriodProgressDTO {
    private KpiProgress kpiProgress;
    private List<DepartmentProgress> departmentProgress;
    private List<OverdueRecord> overdueRecords;

    @Data
    public static class KpiProgress {
        private int totalRecords;
        private int draftingCount;
        private double draftingPercentage;
        private int pendingManagerCount;
        private double pendingManagerPercentage;
        private int pendingApprovalCount;
        private double pendingApprovalPercentage;
        private int completedCount;
        private double completedPercentage;
        private int cancelledCount;
        private double cancelledPercentage;
        private int overdueCount;
        private double overduePercentage;
    }

    @Data
    public static class DepartmentProgress {
        private Long departmentId;
        private String departmentName;
        private int totalRecords;
        private int draftingCount;
        private int pendingManagerCount;
        private int pendingApprovalCount;
        private int completedCount;
        private int cancelledCount;
        private int overdueCount;
    }

    @Data
    public static class OverdueRecord {
        private Long recordId;
        private String employeeName;
        private String employeeEmail;
        private String status;
        private String statusLabel;
        private long overdueDays;
        private Instant deadline;
    }
}
