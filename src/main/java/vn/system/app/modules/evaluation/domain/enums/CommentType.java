package vn.system.app.modules.evaluation.domain.enums;

public enum CommentType {
    SELF_REVIEW,       // Nhân viên tự nhận xét
    MANAGER_FEEDBACK,  // Quản lý trực tiếp nhận xét
    REJECTION_REASON,  // Lý do quản lý gián tiếp trả lại
    APPROVER_OVERRIDE_NOTE // Lý do giải trình khi người duyệt đè điểm quản lý
}
