DELIMITER //
CREATE PROCEDURE create_idx_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_cols VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = p_index
    ) THEN
        SET @ddl = CONCAT('CREATE INDEX `', p_index, '` ON `', p_table, '` (', p_cols, ')');
        PREPARE s FROM @ddl;
        EXECUTE s;
        DEALLOCATE PREPARE s;
    END IF;
END//
DELIMITER ;

CALL create_idx_if_missing('job_position_charts', 'idx_job_position_charts_department', 'department_id');
CALL create_idx_if_missing('permission_categories', 'idx_permission_categories_department_active', 'department_id, active');
CALL create_idx_if_missing('career_paths', 'idx_career_paths_department_active', 'department_id, active');
CALL create_idx_if_missing('department_job_titles', 'idx_department_job_titles_department_active', 'department_id, active');

DROP PROCEDURE IF EXISTS create_idx_if_missing;
