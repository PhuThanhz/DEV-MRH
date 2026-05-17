package vn.system.app.modules.document.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.ScopeSpec;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.document.domain.Document;
import vn.system.app.modules.document.domain.DocumentAccess;
import vn.system.app.modules.document.domain.request.DocumentRequest;
import vn.system.app.modules.document.domain.response.ResDocumentDTO;
import vn.system.app.modules.document.service.DocumentService;
import vn.system.app.common.util.SecurityUtil;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;
import vn.system.app.modules.sharetoken.domain.ShareTokenAccessLog;
import vn.system.app.modules.sharetoken.domain.request.CreateShareTokenRequest;
import vn.system.app.modules.sharetoken.domain.request.SendShareEmailRequest;
import vn.system.app.modules.sharetoken.domain.response.ResShareTokenDTO;
import vn.system.app.modules.sharetoken.service.ProcedureShareTokenService;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;
    private final ProcedureShareTokenService shareTokenService;

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping
    @ApiMessage("Tạo văn bản")
    public ResponseEntity<ResDocumentDTO> create(
            @Valid @RequestBody DocumentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.handleCreate(req));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật văn bản")
    public ResponseEntity<ResDocumentDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest req) {
        return ResponseEntity.ok(service.handleUpdate(id, req));
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================
    @PutMapping("/{id}/active")
    @ApiMessage("Thay đổi trạng thái kích hoạt")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.handleToggleActive(id);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // DELETE
    // =====================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá văn bản")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // MARK AS READ
    // =====================================================
    @PutMapping("/{id}/read")
    @ApiMessage("Đánh dấu đã xem văn bản")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        service.handleMarkAsRead(id);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET ONE
    // =====================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết văn bản")
    public ResponseEntity<ResDocumentDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToDTO(service.fetchById(id)));
    }

    // =====================================================
    // GET ALL (filter + paginate)
    // =====================================================
    @GetMapping
    @ApiMessage("Danh sách văn bản")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<Document> spec,
            Pageable pageable) {
        
        vn.system.app.common.util.UserScopeContext.UserScope scope = vn.system.app.common.util.UserScopeContext.get();
        
        if (scope == null || scope.isSuperAdmin() || scope.isAdminLevel()) {
            // Super Admin & Admin Sub 1 (ALL): Bypass filter, see everything
        } else if (scope.isCompanyLevel()) {
            // Admin Sub 2 (COMPANY): Filter by companyId OR cross-company access
            Specification<Document> companySpec = ScopeSpec.byCompanyScope("department.company.id");
            Specification<Document> accessSpec = buildAccessSpec();
            spec = spec.and(companySpec.or(accessSpec));
        } else {
            // User bình thường (INDIVIDUAL): Filter by createdBy OR accessList
            Specification<Document> individualSpec = (root, query, cb) -> {
                String currentUserLogin = SecurityUtil.getCurrentUserLogin().orElse("");
                String currentUserId = SecurityUtil.getCurrentUserId().orElse("");
                jakarta.persistence.criteria.Predicate createdByPred = cb.equal(root.get("createdBy"), currentUserLogin);
                
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<DocumentAccess> accessRoot = subquery.from(DocumentAccess.class);
                subquery.select(cb.literal(1));
                subquery.where(
                    cb.equal(accessRoot.get("document"), root),
                    cb.equal(accessRoot.get("userId"), currentUserId)
                );
                jakarta.persistence.criteria.Predicate accessPred = cb.exists(subquery);
                
                return cb.or(createdByPred, accessPred);
            };
            spec = spec.and(individualSpec);
        }
        
        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    private Specification<Document> buildAccessSpec() {
        return (root, query, cb) -> {
            String currentUserId = SecurityUtil.getCurrentUserId().orElse("");
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<DocumentAccess> accessRoot = subquery.from(DocumentAccess.class);
            subquery.select(cb.literal(1));
            subquery.where(
                cb.equal(accessRoot.get("document"), root),
                cb.equal(accessRoot.get("userId"), currentUserId)
            );
            return cb.exists(subquery);
        };
    }

    // =====================================================
    // GET BY COMPANY
    // =====================================================
    @GetMapping("/by-company/{companyId}")
    @ApiMessage("Danh sách văn bản theo công ty")
    public ResponseEntity<List<ResDocumentDTO>> getByCompany(
            @PathVariable Long companyId) {
        return ResponseEntity.ok(service.fetchByCompany(companyId));
    }

    // =====================================================
    // GET BY DEPARTMENT
    // =====================================================
    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Danh sách văn bản theo phòng ban")
    public ResponseEntity<List<ResDocumentDTO>> getByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    // =====================================================
    // GET BY SECTION
    // =====================================================
    @GetMapping("/by-section/{sectionId}")
    @ApiMessage("Danh sách văn bản theo bộ phận")
    public ResponseEntity<List<ResDocumentDTO>> getBySection(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(service.fetchBySection(sectionId));
    }

    // =====================================================
    // GET BY CATEGORY
    // =====================================================
    @GetMapping("/by-category/{categoryId}")
    @ApiMessage("Danh sách văn bản theo loại")
    public ResponseEntity<List<ResDocumentDTO>> getByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(service.fetchByCategory(categoryId));
    }

    // =====================================================
    // TẠO SHARE TOKEN
    // =====================================================
    @PostMapping("/{id}/share-tokens")
    @ApiMessage("Tạo link chia sẻ văn bản")
    public ResponseEntity<ResShareTokenDTO> createShareToken(
            @PathVariable Long id,
            @Valid @RequestBody CreateShareTokenRequest req) {
        service.fetchById(id); // kiểm tra document tồn tại
        req.setProcedureType("DOCUMENT");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shareTokenService.handleCreate(id, req));
    }

    // =====================================================
    // DANH SÁCH SHARE TOKEN
    // =====================================================
    @GetMapping("/{id}/share-tokens")
    @ApiMessage("Danh sách link chia sẻ của văn bản")
    public ResponseEntity<List<ResShareTokenDTO>> getShareTokens(
            @PathVariable Long id) {
        return ResponseEntity.ok(shareTokenService.fetchByProcedure(id, "DOCUMENT"));
    }

    // =====================================================
    // THU HỒI SHARE TOKEN
    // =====================================================
    @PatchMapping("/share-tokens/{tokenId}/revoke")
    @ApiMessage("Thu hồi link chia sẻ văn bản")
    public ResponseEntity<Void> revokeShareToken(@PathVariable Long tokenId) {
        shareTokenService.handleRevoke(tokenId);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GỬI EMAIL CHIA SẺ
    // =====================================================
    @PostMapping("/share-tokens/{tokenId}/send-email")
    @ApiMessage("Gửi email chia sẻ văn bản")
    public ResponseEntity<Void> sendShareEmail(
            @PathVariable Long tokenId,
            @Valid @RequestBody SendShareEmailRequest req) {
        shareTokenService.handleSendEmail(tokenId, req.getEmail());
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // LỊCH SỬ TRUY CẬP SHARE TOKEN
    // =====================================================
    @GetMapping("/share-tokens/{tokenId}/access-logs")
    @ApiMessage("Lịch sử truy cập link chia sẻ văn bản")
    public ResponseEntity<List<ShareTokenAccessLog>> getAccessLogs(
            @PathVariable Long tokenId) {
        return ResponseEntity.ok(shareTokenService.fetchAccessLogs(tokenId));
    }
}