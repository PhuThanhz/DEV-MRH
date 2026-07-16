package vn.system.app.modules.evaluation.domain.enums;

public enum RecordStatus {
    NOT_STARTED,             // Chưa bắt đầu
    EMPLOYEE_DRAFTING,       // Nhân viên đang tự đánh giá
    PENDING_MANAGER_REVIEW,  // Nhân viên đã nộp, chờ quản lý trực tiếp chấm
    MANAGER_REVIEWING,       // Quản lý trực tiếp đang chấm (lưu nháp)
    PENDING_APPROVAL,        // Quản lý trực tiếp đã nộp, chờ quản lý gián tiếp phê duyệt
    REVISION_NEEDED,         // Quản lý gián tiếp trả lại, quản lý trực tiếp cần sửa
    COMPLETED,               // Đã được phê duyệt hoàn tất
    CANCELLED                // Đã bị hủy bỏ (nhân viên nghỉ việc hoặc loại khỏi kỳ)
}
