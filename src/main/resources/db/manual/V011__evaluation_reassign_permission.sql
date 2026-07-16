INSERT INTO permissions (
    name,
    api_path,
    method,
    module,
    created_at,
    created_by
)
SELECT
    'Điều chuyển người chấm/duyệt bản đánh giá' AS name,
    '/api/v1/evaluation/records/reassign-evaluator' AS api_path,
    'PATCH' AS method,
    'EVALUATION_MANAGER' AS module,
    NOW() AS created_at,
    'manual-v011' AS created_by
WHERE NOT EXISTS (
    SELECT 1
    FROM permissions
    WHERE api_path = '/api/v1/evaluation/records/reassign-evaluator'
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
JOIN permissions p ON p.api_path = '/api/v1/evaluation/records/reassign-evaluator' AND p.method = 'PATCH'
WHERE r.name IN ('ADMIN_SUB_1', 'ADMIN_SUB_2')
  AND NOT EXISTS (
      SELECT 1
      FROM permission_role pr
      WHERE pr.role_id = r.id
        AND pr.permission_id = p.id
  );
