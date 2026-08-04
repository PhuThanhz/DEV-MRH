-- Chuẩn hóa nội dung báo cáo kết quả để người giao việc nghiệm thu rõ ràng.
-- result_summary: Kết quả đã hoàn thành (bắt buộc).
-- deliverables: Sản phẩm bàn giao / liên kết / bằng chứng.
-- issues: Vướng mắc, rủi ro hoặc nội dung chưa hoàn tất.
-- next_steps: Lưu ý bàn giao, đề xuất hoặc bước tiếp theo.

DELIMITER //
CREATE PROCEDURE add_task_submission_report_sections_if_missing()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'task_submissions'
          AND column_name = 'deliverables'
    ) THEN
        ALTER TABLE task_submissions ADD COLUMN deliverables TEXT NULL AFTER result_summary;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'task_submissions'
          AND column_name = 'issues'
    ) THEN
        ALTER TABLE task_submissions ADD COLUMN issues TEXT NULL AFTER deliverables;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'task_submissions'
          AND column_name = 'next_steps'
    ) THEN
        ALTER TABLE task_submissions ADD COLUMN next_steps TEXT NULL AFTER issues;
    END IF;
END//
DELIMITER ;

CALL add_task_submission_report_sections_if_missing();
DROP PROCEDURE IF EXISTS add_task_submission_report_sections_if_missing;
