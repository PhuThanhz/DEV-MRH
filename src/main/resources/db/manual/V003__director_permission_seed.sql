-- V003: Chuẩn hóa seed permission duyệt Giám đốc cho luồng Bộ chứng từ kế toán.
-- Script idempotent: chạy lại nhiều lần không lỗi, không nhân đôi permission hoặc permission_role.
-- Chỉ tác động bảng permissions/roles/permission_role; không seed user thật, không đụng dữ liệu công ty.
-- company_job_titles / user_positions gán Giám đốc cụ thể cho từng công ty là dữ liệu nghiệp vụ theo môi trường,
-- KHÔNG hardcode trong script này; mỗi môi trường tự cấu hình qua UI hoặc data seed riêng.

-- 1. Thêm permission duyệt Giám đốc nếu chưa tồn tại theo tên nghiệp vụ.
INSERT INTO permissions (
    name,
    api_path,
    method,
    module,
    created_at,
    created_by
)
SELECT
    'Phê duyệt bộ chứng từ kế toán - Giám đốc' AS name,
    '/api/v1/accounting-dossiers/{id}/approve' AS api_path,
    'POST' AS method,
    'ACCOUNTING_DOSSIERS' AS module,
    NOW() AS created_at,
    'manual-v003' AS created_by
WHERE NOT EXISTS (
    SELECT 1
    FROM permissions
    WHERE name = 'Phê duyệt bộ chứng từ kế toán - Giám đốc'
);

-- 2. Gắn permission duyệt Giám đốc vào role DIRECTOR nếu chưa có.
INSERT INTO permission_role (
    role_id,
    permission_id
)
SELECT
    r.id AS role_id,
    p.id AS permission_id
FROM roles r
JOIN permissions p ON p.name = 'Phê duyệt bộ chứng từ kế toán - Giám đốc'
WHERE r.name = 'DIRECTOR'
  AND NOT EXISTS (
      SELECT 1
      FROM permission_role pr
      WHERE pr.role_id = r.id
        AND pr.permission_id = p.id
  );

-- 3. Sửa cấu hình sai từ trước: DEPARTMENT_MANAGER không được giữ quyền duyệt Giám đốc, không phải bóp dữ liệu cho UAT.
DELETE pr
FROM permission_role pr
JOIN roles r ON r.id = pr.role_id
JOIN permissions p ON p.id = pr.permission_id
WHERE r.name = 'DEPARTMENT_MANAGER'
  AND p.name = 'Phê duyệt bộ chứng từ kế toán - Giám đốc';
