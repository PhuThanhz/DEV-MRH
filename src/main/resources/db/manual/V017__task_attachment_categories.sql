-- Phân loại tệp tác vụ:
-- GUIDANCE: tài liệu giao việc do Creator/Admin tải lên.
-- WORKING: tệp làm việc chung do Assignee/Collaborator tải lên.
-- RESULT: tệp thuộc một vòng nộp báo cáo kết quả.

DELIMITER //
CREATE PROCEDURE add_task_attachment_column_if_missing()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'task_attachments'
          AND column_name = 'attachment_category'
    ) THEN
        ALTER TABLE task_attachments
            ADD COLUMN attachment_category VARCHAR(20) NULL AFTER is_result_attachment;
    END IF;
END//

CREATE PROCEDURE add_task_attachment_index_if_missing()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'task_attachments'
          AND index_name = 'idx_attachment_task_category'
    ) THEN
        CREATE INDEX idx_attachment_task_category
            ON task_attachments (task_id, attachment_category);
    END IF;
END//
DELIMITER ;

CALL add_task_attachment_column_if_missing();

UPDATE task_attachments ta
LEFT JOIN task_participants tp
       ON tp.task_id = ta.task_id
      AND tp.user_id = ta.uploaded_by
LEFT JOIN users u ON u.id = ta.uploaded_by
LEFT JOIN roles r ON r.id = u.role_id
SET ta.attachment_category = CASE
    WHEN ta.is_result_attachment = 1 THEN 'RESULT'
    WHEN tp.role = 'CREATOR' OR r.name IN ('SUPER_ADMIN', 'ADMIN_SUB_1') THEN 'GUIDANCE'
    ELSE 'WORKING'
END
WHERE ta.attachment_category IS NULL
   OR ta.attachment_category = '';

ALTER TABLE task_attachments
    MODIFY COLUMN attachment_category VARCHAR(20) NOT NULL;

CALL add_task_attachment_index_if_missing();

DROP PROCEDURE IF EXISTS add_task_attachment_column_if_missing;
DROP PROCEDURE IF EXISTS add_task_attachment_index_if_missing;
