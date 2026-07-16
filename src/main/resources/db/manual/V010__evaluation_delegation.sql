-- V010: T10 — Bảng ủy quyền tạm thời (Delegation) cho module Đánh giá HQCV
-- Manager A ủy quyền cho User B trong khoảng thời gian [startDate, endDate]
-- User B sẽ thấy các bản đánh giá cần chấm của Manager A trong giao diện của mình
-- Manager A vẫn giữ vai trò gốc trên EvaluationRecord (không thay đổi direct_manager_id)

CREATE TABLE IF NOT EXISTS evaluation_delegations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    delegator_id VARCHAR(255) NOT NULL COMMENT 'Manager ủy quyền (người gốc)',
    delegatee_id VARCHAR(255) NOT NULL COMMENT 'Người được ủy quyền',
    start_date   DATETIME(6) NOT NULL COMMENT 'Thời điểm bắt đầu ủy quyền',
    end_date     DATETIME(6) NOT NULL COMMENT 'Thời điểm hết ủy quyền',
    note         TEXT         COMMENT 'Ghi chú lý do ủy quyền',
    active       BOOLEAN      NOT NULL DEFAULT TRUE COMMENT 'FALSE = đã bị hủy bởi Admin hoặc hết hạn thủ công',
    created_by   VARCHAR(255) COMMENT 'ID user tạo delegation (Manager hoặc Admin)',
    created_at   DATETIME(6),
    updated_at   DATETIME(6),

    CONSTRAINT chk_delegation_dates CHECK (end_date > start_date),
    CONSTRAINT chk_delegation_diff_user CHECK (delegator_id <> delegatee_id),

    INDEX idx_del_delegatee_active (delegatee_id, active, start_date, end_date),
    INDEX idx_del_delegator        (delegator_id),
    INDEX idx_del_active_dates     (active, start_date, end_date)
);
