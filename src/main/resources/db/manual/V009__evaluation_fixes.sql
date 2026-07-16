DELIMITER //
CREATE PROCEDURE add_evaluation_column_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE statement_from_ddl FROM @ddl;
        EXECUTE statement_from_ddl;
        DEALLOCATE PREPARE statement_from_ddl;
    END IF;
END//

CREATE PROCEDURE add_evaluation_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_columns VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = p_index
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` (', p_columns, ')');
        PREPARE statement_from_ddl FROM @ddl;
        EXECUTE statement_from_ddl;
        DEALLOCATE PREPARE statement_from_ddl;
    END IF;
END//
DELIMITER ;

-- 1. Thêm cột version cho optimistic locking của evaluation_records
CALL add_evaluation_column_if_missing('evaluation_records', 'version', 'BIGINT NOT NULL DEFAULT 0');

-- 2. Thay đổi performed_by_user_id trong evaluation_history thành nullable
ALTER TABLE `evaluation_history` MODIFY COLUMN `performed_by_user_id` VARCHAR(36) NULL;

-- 3. Tạo composite indexes tối ưu hóa hiệu năng truy vấn
CALL add_evaluation_index_if_missing('evaluation_records', 'idx_records_status_period', '`status`, `period_id`');
CALL add_evaluation_index_if_missing('evaluation_records', 'idx_records_direct_mgr_status', '`direct_manager_id`, `status`');
CALL add_evaluation_index_if_missing('evaluation_records', 'idx_records_indirect_mgr_status', '`indirect_manager_id`, `status`');

CALL add_evaluation_index_if_missing('evaluation_scores', 'idx_scores_record_scored_by', '`evaluation_record_id`, `scored_by`');

CALL add_evaluation_index_if_missing('evaluation_history', 'idx_history_record_performed', '`evaluation_record_id`, `performed_at`');

-- 4. Thêm permission gia hạn deadline đánh giá
INSERT INTO permissions (
    name,
    api_path,
    method,
    module,
    created_at,
    created_by
)
SELECT
    'Gia hạn deadline đánh giá' AS name,
    '/api/v1/evaluation/records/deadline-extension' AS api_path,
    'PATCH' AS method,
    'EVALUATION' AS module,
    NOW() AS created_at,
    'manual-v009' AS created_by
WHERE NOT EXISTS (
    SELECT 1
    FROM permissions
    WHERE api_path = '/api/v1/evaluation/records/deadline-extension'
      AND method = 'PATCH'
);

-- Gán permission này cho ADMIN_SUB_1 và ADMIN_SUB_2
INSERT INTO permission_role (
    role_id,
    permission_id
)
SELECT
    r.id AS role_id,
    p.id AS permission_id
FROM roles r
JOIN permissions p ON p.api_path = '/api/v1/evaluation/records/deadline-extension' AND p.method = 'PATCH'
WHERE r.name IN ('ADMIN_SUB_1', 'ADMIN_SUB_2')
  AND NOT EXISTS (
      SELECT 1
      FROM permission_role pr
      WHERE pr.role_id = r.id
        AND pr.permission_id = p.id
  );

DROP PROCEDURE add_evaluation_column_if_missing;
DROP PROCEDURE add_evaluation_index_if_missing;
