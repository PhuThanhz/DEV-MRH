-- 1. Đảm bảo unique/index cần thiết (do Hibernate ddl-auto=update có thể không tự tạo unique index cho dữ liệu cũ)
-- LƯU Ý: Nếu bảng chưa tồn tại (chạy script trước khi app start), sẽ bỏ qua việc tạo index vì Hibernate sẽ tự tạo bảng cùng index.
SET @dbname = DATABASE();
SET @tablename = "accounting_dossier_outbox";
SET @indexname = "uq_idempotency_key";

-- Kiểm tra xem bảng có tồn tại không
SET @tableExists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = @dbname AND table_name = @tablename);

SET @preparedStatement = (SELECT IF(
  @tableExists = 0,
  "SELECT 'Bảng chưa tồn tại, Hibernate sẽ tạo bảng khi app start' AS Message",
  IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema = @dbname AND table_name = @tablename AND index_name = @indexname) > 0,
    "SELECT 'Index đã tồn tại' AS Message",
    CONCAT("ALTER TABLE ", @tablename, " ADD UNIQUE INDEX ", @indexname, " (idempotency_key)")
  )
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. Backfill bổ sung bước Giám đốc duyệt cho các hồ sơ đang xử lý (SUBMITTED, IN_REVIEW)
-- Chỉ áp dụng cho các hồ sơ chưa có bước Giám đốc (DIRECTOR) và đang ở trạng thái in-progress.
-- Không mở lại hồ sơ đã APPROVED. APPROVED trước release được coi là legacy approved, không mở lại; case Kế toán trưởng vừa duyệt xong trước deploy nằm ngoài backfill tự động và cần xử lý bằng dữ liệu UAT/ops riêng nếu business yêu cầu.
INSERT INTO accounting_dossier_approval_steps (
    dossier_id,
    step_order,
    step_name,
    approver_type,
    approver_user_id,
    status,
    active,
    created_at
)
SELECT 
    ad.id AS dossier_id,
    (SELECT COALESCE(MAX(step_order), 0) + 1 FROM accounting_dossier_approval_steps WHERE dossier_id = ad.id AND active = 1) AS step_order,
    'Giám đốc phê duyệt' AS step_name,
    'DIRECTOR' AS approver_type,
    (
        SELECT IF(COUNT(DISTINCT u.id) = 1, MAX(u.id), NULL)
        FROM users u
        JOIN permission_role pr ON u.role_id = pr.role_id
        JOIN permissions p ON pr.permission_id = p.id
        JOIN user_positions up ON up.user_id = u.id
        LEFT JOIN company_job_titles cjt ON up.company_job_title_id = cjt.id
        LEFT JOIN department_job_titles djt ON up.department_job_title_id = djt.id
        LEFT JOIN departments d ON djt.department_id = d.id
        LEFT JOIN section_job_titles sjt ON up.section_job_title_id = sjt.id
        LEFT JOIN sections s ON sjt.section_id = s.id
        LEFT JOIN departments sd ON s.department_id = sd.id
        WHERE p.name = 'Phê duyệt bộ chứng từ kế toán - Giám đốc'
          AND u.active = 1
          AND up.active = 1
          AND (cjt.company_id = ad.company_id OR d.company_id = ad.company_id OR sd.company_id = ad.company_id)
    ) AS approver_user_id,
    CASE 
      WHEN NOT EXISTS (SELECT 1 FROM accounting_dossier_approval_steps s2 WHERE s2.dossier_id = ad.id AND s2.active = 1 AND s2.status = 'CURRENT') 
      THEN 'CURRENT'
      ELSE 'PENDING'
    END AS status,
    1 AS active,
    NOW() AS created_at
FROM accounting_dossier ad
WHERE ad.status IN ('SUBMITTED', 'IN_REVIEW')
  AND NOT EXISTS (
      SELECT 1 
      FROM accounting_dossier_approval_steps 
      WHERE dossier_id = ad.id 
        AND approver_type = 'DIRECTOR' 
        AND active = 1
  );

-- Đảm bảo trạng thái dossier được set về IN_REVIEW nếu bước Giám đốc trở thành CURRENT (với các hồ sơ đã duyệt xong Kế toán trưởng)
UPDATE accounting_dossier ad
SET ad.status = 'IN_REVIEW'
WHERE ad.status = 'SUBMITTED' 
  AND EXISTS (
      SELECT 1 
      FROM accounting_dossier_approval_steps s 
      WHERE s.dossier_id = ad.id AND s.active = 1 AND s.approver_type = 'DIRECTOR' AND s.status = 'CURRENT'
  );

-- 4. Ghi chú kiểm tra và rollback
-- Để kiểm tra dữ liệu backfill:
-- SELECT * FROM accounting_dossier_approval_steps WHERE approver_type = 'DIRECTOR' ORDER BY created_at DESC;
--
-- Để rollback backfill (nếu xảy ra lỗi logic cấp Giám đốc):
-- DELETE FROM accounting_dossier_approval_steps 
-- WHERE approver_type = 'DIRECTOR' AND status IN ('CURRENT', 'PENDING');
-- Lưu ý: Phải dùng script đổi lại trạng thái IN_REVIEW về SUBMITTED nếu cần thiết.
