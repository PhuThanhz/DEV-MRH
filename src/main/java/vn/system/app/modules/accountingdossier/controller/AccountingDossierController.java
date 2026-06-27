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
import lombok.RequiredArgsConstructor;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierCategory;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierActionRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierCategoryRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierRequest;
import vn.system.app.modules.accountingdossier.domain.request.AccountingDossierDocumentRequest;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierAuditLogDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierCategoryDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingDossierDocumentDTO;
import vn.system.app.modules.accountingdossier.service.AccountingDossierService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting-dossiers")
@RequiredArgsConstructor
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
    public ResponseEntity<ResAccountingDossierDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
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
            Pageable pageable) {
        return ResponseEntity.ok(service.fetchAll(spec, pageable));
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
            @RequestBody AccountingDossierCategoryRequest req) {
        return ResponseEntity.ok(service.toggleCategoryActive(categoryId, req.isActive()));
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
}
