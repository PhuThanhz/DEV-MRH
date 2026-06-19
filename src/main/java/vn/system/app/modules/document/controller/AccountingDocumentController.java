package vn.system.app.modules.document.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.ScopeSpec;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.document.domain.Document;
import vn.system.app.modules.document.domain.request.AccountingDocumentRequest;
import vn.system.app.modules.document.domain.request.DocumentRequest;
import vn.system.app.modules.document.domain.response.ResDocumentDTO;
import vn.system.app.modules.document.service.DocumentService;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.modules.documentcategory.repository.DocumentCategoryRepository;
import vn.system.app.modules.documentcategory.domain.DocumentCategory;
import vn.system.app.modules.document.domain.DocumentAudit;
import vn.system.app.modules.document.repository.DocumentAuditRepository;

import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/accounting-documents")
@RequiredArgsConstructor
public class AccountingDocumentController {

    private static final String ACCOUNTING_CATEGORY_CODE = "ACCOUNTING_DOC";

    private final DocumentService service;
    private final DocumentCategoryRepository categoryRepository;
    private final DocumentAuditRepository auditRepository;

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping
    @ApiMessage("Tạo mới chứng từ kế toán")
    public ResponseEntity<ResDocumentDTO> create(@Valid @RequestBody AccountingDocumentRequest accountingReq) {
        DocumentRequest req = accountingReq.toDocumentRequest(
                getAccountingCategory().getId(),
                generateDocumentCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.handleCreate(req));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật chứng từ kế toán")
    public ResponseEntity<ResDocumentDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountingDocumentRequest accountingReq) {
        assertAccountingDocument(id);
        DocumentRequest req = accountingReq.toDocumentRequest(
                getAccountingCategory().getId(),
                generateDocumentCode());
        return ResponseEntity.ok(service.handleUpdate(id, req));
    }

    // =====================================================
    // DELETE
    // =====================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá chứng từ kế toán")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assertAccountingDocument(id);
        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET ONE
    // =====================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết chứng từ kế toán")
    public ResponseEntity<ResDocumentDTO> getOne(@PathVariable Long id) {
        Document document = assertAccountingDocument(id);
        return ResponseEntity.ok(service.convertToDTO(document));
    }

    // =====================================================
    // GET ALL (filter + paginate)
    // =====================================================
    @GetMapping
    @ApiMessage("Danh sách chứng từ kế toán")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<Document> spec,
            Pageable pageable) {

        // 1. Chỉ lấy các document thuộc kho chứng từ kế toán.
        Specification<Document> accSpec = (root, query, cb) -> cb.equal(root.get("category").get("categoryCode"), ACCOUNTING_CATEGORY_CODE);
        
        // 2. Phân quyền Cross-Company (Level 2)
        vn.system.app.common.util.UserScopeContext.UserScope scope = vn.system.app.common.util.UserScopeContext.get();
        if (scope == null || scope.isSuperAdmin() || scope.isAdminLevel()) {
            // Xem toàn bộ chứng từ kế toán của tập đoàn
        } else {
            // Xem chứng từ của công ty hiện tại
            Specification<Document> companySpec = ScopeSpec.byCompanyScope("department.company.id");
            accSpec = accSpec.and(companySpec);
        }

        spec = spec == null ? accSpec : spec.and(accSpec);
        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    // =====================================================
    // EXPORT
    // =====================================================
    @GetMapping("/export")
    @ApiMessage("Lấy dữ liệu xuất Excel danh sách chứng từ kế toán")
    public ResponseEntity<java.util.List<ResDocumentDTO>> exportExcel(@Filter Specification<Document> spec) {
        Specification<Document> accSpec = (root, query, cb) -> cb.equal(root.get("category").get("categoryCode"), ACCOUNTING_CATEGORY_CODE);
        vn.system.app.common.util.UserScopeContext.UserScope scope = vn.system.app.common.util.UserScopeContext.get();
        if (scope == null || scope.isSuperAdmin() || scope.isAdminLevel()) {
        } else {
            Specification<Document> companySpec = ScopeSpec.byCompanyScope("department.company.id");
            accSpec = accSpec.and(companySpec);
        }
        spec = spec == null ? accSpec : spec.and(accSpec);

        java.util.List<Document> list = service.fetchAllList(spec);
        java.util.List<ResDocumentDTO> dtoList = list.stream().map(service::convertToDTO).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // =====================================================
    // GET AUDITS
    // =====================================================
    @GetMapping("/{id}/audits")
    @ApiMessage("Lịch sử thao tác chứng từ")
    public ResponseEntity<java.util.List<DocumentAudit>> getAudits(@PathVariable Long id) {
        assertAccountingDocument(id);
        return ResponseEntity.ok(auditRepository.findByDocumentIdOrderByCreatedAtDesc(id));
    }

    private DocumentCategory getAccountingCategory() {
        return categoryRepository.findByCategoryCode(ACCOUNTING_CATEGORY_CODE)
                .orElseThrow(() -> new IdInvalidException("Danh mục hệ thống cho chứng từ kế toán chưa được khởi tạo"));
    }

    private Document assertAccountingDocument(Long id) {
        Document document = service.fetchById(id);
        if (document.getCategory() == null
                || !ACCOUNTING_CATEGORY_CODE.equals(document.getCategory().getCategoryCode())) {
            throw new IdInvalidException("Chứng từ kế toán không tồn tại");
        }
        return document;
    }

    private String generateDocumentCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "CT-" + datePart + "-" + randomPart;
    }
}
