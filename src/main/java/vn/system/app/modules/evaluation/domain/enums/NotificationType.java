package vn.system.app.modules.evaluation.domain.enums;

public enum NotificationType {
    PERIOD_OPENED,         // Kỳ đánh giá đã mở
    REMINDER_DEADLINE,     // Nhắc deadline
    MANAGER_REVIEW_NEEDED, // Có form nhân viên cần chấm
    APPROVAL_NEEDED,       // Có form cần phê duyệt
    RESULT_AVAILABLE,      // Kết quả đã có, nhân viên có thể xem
    REVISION_NEEDED        // Form bị trả lại, quản lý trực tiếp cần sửa
}
