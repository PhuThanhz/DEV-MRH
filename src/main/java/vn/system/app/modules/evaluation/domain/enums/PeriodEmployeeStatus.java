package vn.system.app.modules.evaluation.domain.enums;

public enum PeriodEmployeeStatus {
    ACTIVE,    // Nhân viên đang tham gia kỳ bình thường
    CANCELLED  // Nhân viên nghỉ việc giữa kỳ, bản đánh giá bị hủy (giữ nguyên dữ liệu để audit)
}
