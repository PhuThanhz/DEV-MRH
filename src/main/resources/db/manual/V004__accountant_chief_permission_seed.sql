-- V004: Chuẩn hóa permission nghiệp vụ cho bước Kế toán và Kế toán trưởng trong luồng Bộ chứng từ kế toán.
-- Script idempotent: chạy lại nhiều lần không lỗi, không nhân đôi permission hoặc permission_role.
-- Chỉ tác động bảng permissions/roles/permission_role; không seed user thật, không đụng dữ liệu công ty.
-- Role thực tế đã kiểm tra trên hrm_0107: KETOAN (Kế toán viên), KETOANTRUONG (Kế toán trưởng).

-- 1. Thêm permission duyệt bước Kế toán nếu chưa tồn tại theo tên nghiệp vụ.
INSERT INTO permissions (
    name,
    api_path,
    method,
    module,
    created_at,
    created_by
)
SELECT
    'Phê duyệt bộ chứng từ kế toán - Kế toán' AS name,
    '/api/v1/accounting-dossiers/{id}/approve' AS api_path,
    'POST' AS method,
    'ACCOUNTING_DOSSIERS' AS module,
    NOW() AS created_at,
    'manual-v004' AS created_by
WHERE NOT EXISTS (
    SELECT 1
    FROM permissions
    WHERE name = 'Phê duyệt bộ chứng từ kế toán - Kế toán'
);

-- 2. Thêm permission duyệt bước Kế toán trưởng nếu chưa tồn tại theo tên nghiệp vụ.
INSERT INTO permissions (
    name,
    api_path,
    method,
    module,
    created_at,
    created_by
)
SELECT
    'Phê duyệt bộ chứng từ kế toán - Kế toán trưởng' AS name,
    '/api/v1/accounting-dossiers/{id}/approve' AS api_path,
    'POST' AS method,
    'ACCOUNTING_DOSSIERS' AS module,
    NOW() AS created_at,
    'manual-v004' AS created_by
WHERE NOT EXISTS (
    SELECT 1
    FROM permissions
    WHERE name = 'Phê duyệt bộ chứng từ kế toán - Kế toán trưởng'
);

-- 3. Gắn permission Kế toán vào role KETOAN nếu chưa có.
INSERT INTO permission_role (
    role_id,
    permission_id
)
SELECT
    r.id AS role_id,
    p.id AS permission_id
FROM roles r
JOIN permissions p ON p.name = 'Phê duyệt bộ chứng từ kế toán - Kế toán'
WHERE r.name = 'KETOAN'
  AND NOT EXISTS (
      SELECT 1
      FROM permission_role pr
      WHERE pr.role_id = r.id
        AND pr.permission_id = p.id
  );

-- 4. Gắn permission Kế toán trưởng vào role KETOANTRUONG nếu chưa có.
INSERT INTO permission_role (
    role_id,
    permission_id
)
SELECT
    r.id AS role_id,
    p.id AS permission_id
FROM roles r
JOIN permissions p ON p.name = 'Phê duyệt bộ chứng từ kế toán - Kế toán trưởng'
WHERE r.name = 'KETOANTRUONG'
  AND NOT EXISTS (
      SELECT 1
      FROM permission_role pr
      WHERE pr.role_id = r.id
        AND pr.permission_id = p.id
  );

-- 5. Admin có thể có quyền quản trị khác nhưng không giữ 2 permission người duyệt nghiệp vụ này.
DELETE pr
FROM permission_role pr
JOIN roles r ON r.id = pr.role_id
JOIN permissions p ON p.id = pr.permission_id
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN_SUB_1', 'ADMIN_SUB_2')
  AND p.name IN (
      'Phê duyệt bộ chứng từ kế toán - Kế toán',
      'Phê duyệt bộ chứng từ kế toán - Kế toán trưởng'
  );
