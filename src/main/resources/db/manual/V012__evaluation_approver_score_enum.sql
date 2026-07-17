-- Allow the final evaluator to save scores in evaluation_scores.
-- MySQL enum columns are not always expanded by Hibernate ddl-auto=update.

SET @scored_by_type := (
    SELECT COLUMN_TYPE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evaluation_scores'
      AND COLUMN_NAME = 'scored_by'
);

SET @sql := IF(
    @scored_by_type LIKE '%APPROVER%',
    'SELECT ''evaluation_scores.scored_by already supports APPROVER'' AS message',
    'ALTER TABLE evaluation_scores MODIFY scored_by ENUM(''EMPLOYEE'', ''MANAGER'', ''APPROVER'') NOT NULL'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
