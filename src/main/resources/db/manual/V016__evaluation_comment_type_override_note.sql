-- Allow approver override reasons to be stored in evaluation_comments.
-- Hibernate ddl-auto=update does not reliably expand existing MySQL enum columns.

SET @comment_type := (
    SELECT COLUMN_TYPE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'evaluation_comments'
      AND COLUMN_NAME = 'comment_type'
);

SET @sql := IF(
    @comment_type LIKE '%APPROVER_OVERRIDE_NOTE%',
    'SELECT ''evaluation_comments.comment_type already supports APPROVER_OVERRIDE_NOTE'' AS message',
    'ALTER TABLE evaluation_comments MODIFY comment_type ENUM(''SELF_REVIEW'', ''MANAGER_FEEDBACK'', ''REJECTION_REASON'', ''APPROVER_OVERRIDE_NOTE'') NOT NULL'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
