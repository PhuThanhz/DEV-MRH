package vn.system.app.modules.accountingdossier.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierCategory;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierDocumentCheckRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierCategoryRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierCategoryActiveRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierSubmitRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierDocumentRequest;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierAuditLogDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierBulkActionDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierCategoryDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDocumentDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierApprovalStepDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierReportRowDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierStorageSummaryDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDashboardMetricsDTO;
import vn.system.app.modules.accountingdossier.service.AccountingDossierService;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting-dossiers")
@RequiredArgsConstructor
@Validated
public class AccountingDossierController {

    private final AccountingDossierService service;

    @PostMapping
    @ApiMessage("Tạo mới bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> create(
            @Valid @RequestBody AccountingDossierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountingDossierRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa bộ chứng từ kế toán")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @ApiMessage("Chi tiết bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping("/{id}/submit")
    @ApiMessage("Chuyển xử lý bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> submit(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AccountingDossierSubmitRequest req) {
        return ResponseEntity.ok(service.submit(id, req));
    }

    @PostMapping("/{id}/request-return")
    @ApiMessage("Yêu cầu hoàn bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> requestReturn(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.requestReturn(id, req));
    }

    @GetMapping("/{id}/logs")
    @ApiMessage("Nhật ký bộ chứng từ kế toán")
    public ResponseEntity<List<ResAccountingDossierAuditLogDTO>> getLogs(@PathVariable Long id) {
        return ResponseEntity.ok(service.fetchLogs(id));
    }

    @GetMapping
    @ApiMessage("Danh sách bộ chứng từ kế toán")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<AccountingDossier> spec,
            Pageable pageable,
            @RequestParam(required = false) String approverUserId,
            @RequestParam(required = false) String storageStatus,
            @RequestParam(required = false) Integer retentionYear,
            @RequestParam(required = false) Integer retentionMonth,
            @RequestParam(required = false) Integer retentionDay,
            @RequestParam(required = false) String creatorId,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long dossierCategoryId) {
        return ResponseEntity.ok(service.fetchAll(
                spec,
                pageable,
                approverUserId,
                storageStatus,
                retentionYear,
                retentionMonth,
                retentionDay,
                creatorId,
                companyId,
                departmentId,
                dossierCategoryId));
    }

    @GetMapping("/pending-my-approval")
    @ApiMessage("Danh sách bộ chứng từ chờ tôi duyệt")
    public ResponseEntity<ResultPaginationDTO> getPendingMyApproval(Pageable pageable) {
        return ResponseEntity.ok(service.fetchPendingMyApproval(pageable));
    }

    @GetMapping("/documents")
    @ApiMessage("Danh sách chứng từ con trong các bộ chứng từ")
    public ResponseEntity<ResultPaginationDTO> getAllDossierDocuments(
            @Filter Specification<vn.system.app.modules.accountingdossier.domain.AccountingDossierDocument> spec,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dossierCode,
            @RequestParam(required = false) String fileStatus,
            Pageable pageable) {
        return ResponseEntity.ok(service.fetchAllDossierDocuments(spec, keyword, dossierCode, fileStatus, pageable));
    }

    @PostMapping("/storage/refresh-expired")
    @ApiMessage("Cập nhật trạng thái hết thời hạn lưu trữ")
    public ResponseEntity<Integer> refreshExpiredStorageStatus() {
        return ResponseEntity.ok(service.refreshExpiredRetentionStatusesByUser());
    }

    @GetMapping("/dashboard/summary")
    @ApiMessage("Tổng quan lưu trữ bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierStorageSummaryDTO> getStorageSummary(
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(service.getStorageSummary(companyId));
    }

    @GetMapping("/dashboard/metrics")
    @ApiMessage("Tổng hợp biểu đồ thống kê bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDashboardMetricsDTO> getDashboardMetrics(
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(service.getDashboardMetrics(companyId));
    }

    @GetMapping("/dashboard/pending-by-role")
    @ApiMessage("Thống kê bộ chứng từ đang chờ duyệt theo vai trò")
    public ResponseEntity<List<ResAccountingDossierReportRowDTO>> getPendingByRole(
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(service.pendingByRole(companyId));
    }

    @GetMapping("/reports/by-status")
    @ApiMessage("Báo cáo bộ chứng từ theo trạng thái")
    public ResponseEntity<List<ResAccountingDossierReportRowDTO>> reportByStatus(
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(service.reportByStatus(companyId));
    }

    @GetMapping("/reports/by-department")
    @ApiMessage("Báo cáo bộ chứng từ theo phòng ban")
    public ResponseEntity<List<ResAccountingDossierReportRowDTO>> reportByDepartment(
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(service.reportByDepartment(companyId));
    }

    @GetMapping("/reports/by-category")
    @ApiMessage("Báo cáo bộ chứng từ theo danh mục")
    public ResponseEntity<List<ResAccountingDossierReportRowDTO>> reportByCategory(
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(service.reportByCategory(companyId));
    }

    // ==================== DOSSIER TEMPLATES ====================

    @GetMapping("/categories")
    @ApiMessage("Danh sách mẫu bộ chứng từ kế toán")
    public ResponseEntity<ResultPaginationDTO> getCategories(
            @Filter Specification<AccountingDossierCategory> spec,
            Pageable pageable) {
        return ResponseEntity.ok(service.fetchCategories(spec, pageable));
    }

    @GetMapping("/categories/active")
    @ApiMessage("Danh sách mẫu bộ chứng từ kế toán đang dùng")
    public ResponseEntity<List<ResAccountingDossierCategoryDTO>> getActiveCategories() {
        return ResponseEntity.ok(service.fetchActiveCategories());
    }

    @PostMapping("/categories")
    @ApiMessage("Tạo mẫu bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierCategoryDTO> createCategory(
            @Valid @RequestBody AccountingDossierCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCategory(req));
    }

    @PutMapping("/categories/{categoryId}")
    @ApiMessage("Cập nhật mẫu bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierCategoryDTO> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody AccountingDossierCategoryRequest req) {
        return ResponseEntity.ok(service.updateCategory(categoryId, req));
    }

    @PutMapping("/categories/{categoryId}/active")
    @ApiMessage("Bật/tắt mẫu bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierCategoryDTO> toggleCategoryActive(
            @PathVariable Long categoryId,
            @Valid @RequestBody AccountingDossierCategoryActiveRequest req) {
        return ResponseEntity.ok(service.toggleCategoryActive(categoryId, Boolean.TRUE.equals(req.getActive())));
    }

    @DeleteMapping("/categories/{categoryId}")
    @ApiMessage("Xóa mẫu bộ chứng từ kế toán")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        service.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    // ==================== DOCUMENT ITEMS ====================

    @GetMapping("/{id}/documents")
    @ApiMessage("Danh sách chứng từ con trong bộ")
    public ResponseEntity<List<ResAccountingDossierDocumentDTO>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(service.fetchAllDocuments(id));
    }

    @PostMapping("/{id}/documents")
    @ApiMessage("Thêm chứng từ con vào bộ")
    public ResponseEntity<ResAccountingDossierDocumentDTO> addDocument(
            @PathVariable Long id,
            @Valid @RequestBody AccountingDossierDocumentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addDocument(id, req));
    }

    @PutMapping("/{id}/documents/{docId}")
    @ApiMessage("Sửa chứng từ con trong bộ")
    public ResponseEntity<ResAccountingDossierDocumentDTO> updateDocument(
            @PathVariable Long id,
            @PathVariable Long docId,
            @Valid @RequestBody AccountingDossierDocumentRequest req) {
        return ResponseEntity.ok(service.updateDocument(id, docId, req));
    }

    @DeleteMapping("/{id}/documents/{docId}")
    @ApiMessage("Xóa chứng từ con trong bộ")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long docId) {
        service.deleteDocument(id, docId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/documents/{docId}/check")
    @ApiMessage("Kiểm tra chứng từ con trong bộ")
    public ResponseEntity<ResAccountingDossierDocumentDTO> reviewDocument(
            @PathVariable Long id,
            @PathVariable Long docId,
            @Valid @RequestBody AccountingDossierDocumentCheckRequest req) {
        return ResponseEntity.ok(service.reviewDocument(id, docId, req));
    }

    @PostMapping("/{id}/approve")
    @ApiMessage("Phê duyệt bước bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> approve(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.approve(id, req));
    }

    @PostMapping("/{id}/claim")
    @ApiMessage("Nhận xử lý bước duyệt bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> claim(@PathVariable Long id) {
        return ResponseEntity.ok(service.claim(id));
    }

    @PostMapping("/{id}/reject")
    @ApiMessage("Từ chối bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> reject(
            @PathVariable Long id,
            @Valid @RequestBody AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.reject(id, req));
    }

    @PostMapping("/{id}/terminate")
    @ApiMessage("Chấm dứt bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierDTO> terminate(
            @PathVariable Long id,
            @Valid @RequestBody AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.terminate(id, req));
    }

    @PostMapping("/{id}/reopen")
    @ApiMessage("Mở lại bộ chứng từ kế toán bị từ chối")
    public ResponseEntity<ResAccountingDossierDTO> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(service.reopen(id));
    }

    @PostMapping("/{id}/archive")
    @ApiMessage("Đưa bộ chứng từ kế toán vào lưu trữ")
    public ResponseEntity<ResAccountingDossierDTO> archive(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.archive(id, req));
    }

    @PostMapping("/{id}/return-response")
    @ApiMessage("Phản hồi yêu cầu hoàn bộ chứng từ")
    public ResponseEntity<ResAccountingDossierDTO> handleReturnResponse(
            @PathVariable Long id,
            @RequestParam String action,
            @Valid @RequestBody(required = false) AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.handleReturnResponse(id, action, req));
    }

    @PostMapping("/{id}/sync-template/reject")
    @ApiMessage("Từ chối đồng bộ bộ chứng từ phi cấu trúc thành mẫu")
    public ResponseEntity<ResAccountingDossierDTO> rejectTemplateSync(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.rejectTemplateSync(id, req));
    }

    @GetMapping("/{id}/approval-steps")
    @ApiMessage("Danh sách tiến trình duyệt bộ chứng từ")
    public ResponseEntity<List<ResAccountingDossierApprovalStepDTO>> getApprovalSteps(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getApprovalSteps(id));
    }

    @PostMapping("/bulk-approve")
    @ApiMessage("Duyệt hàng loạt bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierBulkActionDTO> bulkApprove(
            @RequestParam @Size(max = 200, message = "Số lượng ID xử lý hàng loạt tối đa là 200") List<Long> ids,
            @Valid @RequestBody(required = false) AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.bulkApprove(ids, req));
    }

    @PostMapping("/bulk/approve")
    @ApiMessage("Duyệt hàng loạt bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierBulkActionDTO> bulkApprovePlanRoute(
            @RequestParam @Size(max = 200, message = "Số lượng ID xử lý hàng loạt tối đa là 200") List<Long> ids,
            @Valid @RequestBody(required = false) AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.bulkApprove(ids, req));
    }

    @PostMapping("/bulk/reject")
    @ApiMessage("Từ chối hàng loạt bộ chứng từ kế toán")
    public ResponseEntity<ResAccountingDossierBulkActionDTO> bulkReject(
            @RequestParam @Size(max = 200, message = "Số lượng ID xử lý hàng loạt tối đa là 200") List<Long> ids,
            @Valid @RequestBody AccountingDossierActionRequest req) {
        return ResponseEntity.ok(service.bulkReject(ids, req));
    }

    @PostMapping("/{id}/documents/bulk-check")
    @ApiMessage("Kiểm tra hàng loạt chứng từ con")
    public ResponseEntity<ResAccountingDossierBulkActionDTO> bulkCheckDocuments(
            @PathVariable Long id,
            @RequestParam @Size(max = 200, message = "Số lượng ID xử lý hàng loạt tối đa là 200") List<Long> documentIds,
            @RequestParam String checkStatus,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(service.bulkCheckDocuments(id, documentIds, checkStatus, note));
    }

    @PostMapping("/{id}/documents/bulk/check")
    @ApiMessage("Kiểm tra hàng loạt chứng từ con")
    public ResponseEntity<ResAccountingDossierBulkActionDTO> bulkCheckDocumentsPlanRoute(
            @PathVariable Long id,
            @RequestParam @Size(max = 200, message = "Số lượng ID xử lý hàng loạt tối đa là 200") List<Long> documentIds,
            @RequestParam String checkStatus,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(service.bulkCheckDocuments(id, documentIds, checkStatus, note));
    }

    @GetMapping("/qr/{token}")
    @ApiMessage("Tra cứu bộ chứng từ qua mã QR")
    public ResponseEntity<ResAccountingDossierDTO> getByQrToken(@PathVariable String token) {
        return ResponseEntity.ok(service.getByQrToken(token));
    }

    @PostMapping("/{id}/reassign-director")
    @ApiMessage("Emergency Reassign Giám đốc phê duyệt")
    public ResponseEntity<ResAccountingDossierDTO> reassignDirector(
            @PathVariable Long id,
            @RequestParam String newApproverUserId,
            @RequestParam String reason) {
        return ResponseEntity.ok(service.reassignDirector(id, newApproverUserId, reason));
    }
}
