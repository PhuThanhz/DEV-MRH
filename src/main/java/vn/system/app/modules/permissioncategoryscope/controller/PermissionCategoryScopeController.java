package vn.system.app.modules.permissioncategoryscope.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.modules.permissioncategoryscope.domain.request.PermissionCategoryScopeRequest;
import vn.system.app.modules.permissioncategoryscope.domain.response.ResPermissionCategoryScopeGroupedDTO;
import vn.system.app.modules.permissioncategoryscope.service.PermissionCategoryScopeService;

@RestController
@RequestMapping("/api/v1/permission-category-scopes")
public class PermissionCategoryScopeController {

    private final PermissionCategoryScopeService service;

    public PermissionCategoryScopeController(PermissionCategoryScopeService service) {
        this.service = service;
    }

    // ========================================
    // POST – TẠO / GHI CẤU HÌNH (UPSERT)
    // ========================================
    @PostMapping
    public ResponseEntity<ResPermissionCategoryScopeGroupedDTO> create(
            @Valid @RequestBody PermissionCategoryScopeRequest req) {

        ResPermissionCategoryScopeGroupedDTO result = service.createOrUpdate(
                req.getPermissionCategoryId(),
                req.getDepartmentId(),
                req.getDepartmentJobTitleIds());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    // ========================================
    // PUT – CẬP NHẬT CẤU HÌNH (REST CHUẨN)
    // ========================================
    @PutMapping
    public ResponseEntity<ResPermissionCategoryScopeGroupedDTO> update(
            @Valid @RequestBody PermissionCategoryScopeRequest req) {

        ResPermissionCategoryScopeGroupedDTO result = service.createOrUpdate(
                req.getPermissionCategoryId(),
                req.getDepartmentId(),
                req.getDepartmentJobTitleIds());

        return ResponseEntity.ok(result);
    }

    // ========================================
    // GET – LOAD CHO UI
    // ========================================
    @GetMapping("/grouped")
    public ResponseEntity<ResPermissionCategoryScopeGroupedDTO> getGrouped(
            @RequestParam Long permissionCategoryId,
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(
                service.fetchGrouped(permissionCategoryId, departmentId));
    }

    // ========================================
    // DELETE – XOÁ CẤU HÌNH
    // ========================================
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestParam Long permissionCategoryId,
            @RequestParam Long departmentId) {

        service.delete(permissionCategoryId, departmentId);
        return ResponseEntity.noContent().build();
    }
}
