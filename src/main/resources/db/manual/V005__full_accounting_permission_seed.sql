-- =============================================================================
-- V005: Seed đầy đủ toàn bộ permission cho các module Kế toán.
-- Modules: ACCOUNTING_DOSSIERS, ACCOUNTING_WORKFLOWS, ACCOUNTING_DELEGATIONS
-- Script idempotent: dùng INSERT ... WHERE NOT EXISTS theo (method, api_path).
-- Chạy lại nhiều lần không lỗi, không nhân đôi dữ liệu.
-- Không gán permission_role ở đây — admin tự gán qua giao diện phân quyền.
-- =============================================================================

-- ======================== ACCOUNTING_DOSSIERS ========================

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem danh sách bộ chứng từ', '/api/v1/accounting-dossiers', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem chi tiết bộ chứng từ', '/api/v1/accounting-dossiers/{id}', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/{id}');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem bộ chứng từ qua QR', '/api/v1/accounting-dossiers/qr/{token}', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/qr/{token}');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Tạo bộ chứng từ', '/api/v1/accounting-dossiers', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Cập nhật bộ chứng từ', '/api/v1/accounting-dossiers/{id}', 'PUT', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'PUT' AND api_path = '/api/v1/accounting-dossiers/{id}');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xóa bộ chứng từ', '/api/v1/accounting-dossiers/{id}', 'DELETE', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'DELETE' AND api_path = '/api/v1/accounting-dossiers/{id}');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Chuyển bộ chứng từ để duyệt', '/api/v1/accounting-dossiers/{id}/submit', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/submit');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Phê duyệt bộ chứng từ', '/api/v1/accounting-dossiers/{id}/approve', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/approve');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Từ chối bộ chứng từ', '/api/v1/accounting-dossiers/{id}/reject', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/reject');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Chấm dứt xử lý bộ chứng từ', '/api/v1/accounting-dossiers/{id}/terminate', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/terminate');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Đưa bộ chứng từ vào lưu trữ', '/api/v1/accounting-dossiers/{id}/archive', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/archive');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Yêu cầu hoàn trả bộ chứng từ', '/api/v1/accounting-dossiers/{id}/request-return', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/request-return');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Phản hồi yêu cầu hoàn trả', '/api/v1/accounting-dossiers/{id}/return-response', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/return-response');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem nhật ký lịch sử bộ chứng từ', '/api/v1/accounting-dossiers/{id}/logs', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/{id}/logs');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem danh mục bộ chứng từ', '/api/v1/accounting-dossiers/categories', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/categories');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem danh mục bộ chứng từ đang hoạt động', '/api/v1/accounting-dossiers/categories/active', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/categories/active');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Tạo danh mục bộ chứng từ', '/api/v1/accounting-dossiers/categories', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/categories');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Cập nhật danh mục bộ chứng từ', '/api/v1/accounting-dossiers/categories/{categoryId}', 'PUT', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'PUT' AND api_path = '/api/v1/accounting-dossiers/categories/{categoryId}');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Bật/tắt danh mục bộ chứng từ', '/api/v1/accounting-dossiers/categories/{categoryId}/active', 'PUT', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'PUT' AND api_path = '/api/v1/accounting-dossiers/categories/{categoryId}/active');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem tài liệu đính kèm bộ chứng từ', '/api/v1/accounting-dossiers/{id}/documents', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/{id}/documents');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Thêm tài liệu vào bộ chứng từ', '/api/v1/accounting-dossiers/{id}/documents', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/documents');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Cập nhật tài liệu bộ chứng từ', '/api/v1/accounting-dossiers/{id}/documents/{docId}', 'PUT', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'PUT' AND api_path = '/api/v1/accounting-dossiers/{id}/documents/{docId}');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xóa tài liệu bộ chứng từ', '/api/v1/accounting-dossiers/{id}/documents/{docId}', 'DELETE', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'DELETE' AND api_path = '/api/v1/accounting-dossiers/{id}/documents/{docId}');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Kiểm tra tài liệu bộ chứng từ', '/api/v1/accounting-dossiers/{id}/documents/{docId}/check', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/documents/{docId}/check');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Kiểm tra hàng loạt tài liệu', '/api/v1/accounting-dossiers/{id}/documents/bulk/check', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/documents/bulk/check');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem bước phê duyệt bộ chứng từ', '/api/v1/accounting-dossiers/{id}/approval-steps', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/{id}/approval-steps');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem bộ chứng từ chờ tôi duyệt', '/api/v1/accounting-dossiers/pending-my-approval', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/pending-my-approval');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Phê duyệt hàng loạt bộ chứng từ', '/api/v1/accounting-dossiers/bulk/approve', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/bulk/approve');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Từ chối hàng loạt bộ chứng từ', '/api/v1/accounting-dossiers/bulk/reject', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/bulk/reject');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem thống kê lưu trữ tổng hợp', '/api/v1/accounting-dossiers/dashboard/summary', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/dashboard/summary');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem thống kê chờ duyệt theo vai trò', '/api/v1/accounting-dossiers/dashboard/pending-by-role', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/dashboard/pending-by-role');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Báo cáo bộ chứng từ theo trạng thái', '/api/v1/accounting-dossiers/reports/by-status', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/reports/by-status');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Báo cáo bộ chứng từ theo phòng ban', '/api/v1/accounting-dossiers/reports/by-department', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/reports/by-department');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Báo cáo bộ chứng từ theo danh mục', '/api/v1/accounting-dossiers/reports/by-category', 'GET', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-dossiers/reports/by-category');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Cập nhật trạng thái lưu trữ hết hạn', '/api/v1/accounting-dossiers/storage/refresh-expired', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/storage/refresh-expired');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Từ chối đồng bộ luồng duyệt', '/api/v1/accounting-dossiers/{id}/sync-template/reject', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/sync-template/reject');

-- ======================== ACCOUNTING_WORKFLOWS ========================

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem danh sách cấu hình luồng duyệt', '/api/v1/accounting-approval-workflows', 'GET', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-approval-workflows');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Tạo nháp cấu hình luồng duyệt', '/api/v1/accounting-approval-workflows', 'POST', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-workflows');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Cập nhật nháp cấu hình luồng duyệt', '/api/v1/accounting-approval-workflows/{id}/draft', 'PUT', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'PUT' AND api_path = '/api/v1/accounting-approval-workflows/{id}/draft');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Kiểm tra hợp lệ cấu hình luồng duyệt', '/api/v1/accounting-approval-workflows/{id}/validate', 'POST', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-workflows/{id}/validate');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Áp dụng cấu hình luồng duyệt', '/api/v1/accounting-approval-workflows/{id}/publish', 'POST', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-workflows/{id}/publish');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Ngưng áp dụng cấu hình luồng duyệt', '/api/v1/accounting-approval-workflows/{id}/deactivate', 'POST', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-workflows/{id}/deactivate');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Kích hoạt lại cấu hình luồng duyệt', '/api/v1/accounting-approval-workflows/{id}/reactivate', 'POST', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-workflows/{id}/reactivate');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Sao chép cấu hình luồng duyệt thành nháp', '/api/v1/accounting-approval-workflows/{id}/copy', 'POST', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-workflows/{id}/copy');

-- ======================== ACCOUNTING_DELEGATIONS ========================

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem danh sách ủy quyền phê duyệt', '/api/v1/accounting-approval-delegations', 'GET', 'ACCOUNTING_DELEGATIONS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'GET' AND api_path = '/api/v1/accounting-approval-delegations');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Tạo ủy quyền phê duyệt', '/api/v1/accounting-approval-delegations', 'POST', 'ACCOUNTING_DELEGATIONS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-delegations');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Kích hoạt ủy quyền phê duyệt', '/api/v1/accounting-approval-delegations/{id}/activate', 'POST', 'ACCOUNTING_DELEGATIONS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-delegations/{id}/activate');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Thu hồi ủy quyền phê duyệt', '/api/v1/accounting-approval-delegations/{id}/revoke', 'POST', 'ACCOUNTING_DELEGATIONS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-delegations/{id}/revoke');

-- ======================== MISSING ROUTE PERMISSIONS ========================
-- Các route dưới đây đã có controller/service nhưng chưa có permission để admin gán role.

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Nhận xử lý bước duyệt bộ chứng từ', '/api/v1/accounting-dossiers/{id}/claim', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/claim');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Phê duyệt hàng loạt bộ chứng từ', '/api/v1/accounting-dossiers/bulk-approve', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/bulk-approve');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Kiểm tra hàng loạt chứng từ con', '/api/v1/accounting-dossiers/{id}/documents/bulk-check', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/documents/bulk-check');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Đổi Giám đốc duyệt khẩn cấp', '/api/v1/accounting-dossiers/{id}/reassign-director', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-dossiers/{id}/reassign-director');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Xem trước luồng duyệt bộ chứng từ', '/api/v1/accounting-approval-workflows/dossiers/{dossierId}/preview', 'POST', 'ACCOUNTING_WORKFLOWS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-workflows/dossiers/{dossierId}/preview');

INSERT INTO permissions (name, api_path, method, module, created_at, created_by)
SELECT 'Quét quá hạn SLA duyệt bộ chứng từ', '/api/v1/accounting-approval-sla/scan-overdue', 'POST', 'ACCOUNTING_DOSSIERS', NOW(), 'seed-v005'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE method = 'POST' AND api_path = '/api/v1/accounting-approval-sla/scan-overdue');
