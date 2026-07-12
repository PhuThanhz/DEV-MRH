-- Dọn dẹp dữ liệu cũ bị sai chính tả (nếu có)
UPDATE accounting_dossier_approval_steps 
SET approver_type = 'ACCOUNTANT' 
WHERE approver_type = 'ACOUNTANT';

UPDATE accounting_dossier_approval_steps 
SET approver_type = 'CHIEF_ACCOUNTANT' 
WHERE approver_type = 'CHIEF_ACOUNTANT';

-- Tạo bảng accounting_manual_migration_history
CREATE TABLE IF NOT EXISTS accounting_manual_migration_history (
    version VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    executed_at DATETIME NOT NULL,
    executed_by VARCHAR(100) NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    success TINYINT(1) NOT NULL,
    environment VARCHAR(50) NOT NULL,
    PRIMARY KEY (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
