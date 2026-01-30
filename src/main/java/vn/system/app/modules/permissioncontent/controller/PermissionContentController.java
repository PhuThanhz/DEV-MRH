package vn.system.app.modules.permissioncontent.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.permissioncontent.domain.request.ReqCreatePermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.request.ReqUpdatePermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.response.ResPermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.response.ResPermissionContentWithScopeDTO;
import vn.system.app.modules.permissioncontent.service.PermissionContentService;

@RestController
@RequestMapping("/api/v1/permission-contents")
public class PermissionContentController {

    private final PermissionContentService service;

    public PermissionContentController(PermissionContentService service) {
        this.service = service;
    }

    // ==================================================
    // CREATE
    // ==================================================
    @PostMapping
    @ApiMessage("Tạo nội dung quyền")
    public ResponseEntity<ResPermissionContentWithScopeDTO> create(
            @Valid @RequestBody ReqCreatePermissionContentDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(req));
    }

    // ==================================================
    // GET LIST BY CATEGORY
    // ==================================================
    @GetMapping
    @ApiMessage("Danh sách nội dung quyền theo danh mục")
    public ResponseEntity<List<ResPermissionContentDTO>> fetchByCategory(
            @RequestParam("categoryId") Long categoryId) {

        return ResponseEntity.ok(
                service.fetchByCategory(categoryId));
    }

    // ==================================================
    // GET DETAIL
    // ==================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết nội dung quyền")
    public ResponseEntity<ResPermissionContentWithScopeDTO> fetchDetail(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(
                service.fetchDetail(id));
    }

    // ==================================================
    // UPDATE
    // ==================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật nội dung quyền")
    public ResponseEntity<ResPermissionContentWithScopeDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqUpdatePermissionContentDTO req) {

        return ResponseEntity.ok(
                service.update(id, req));
    }

    // ==================================================
    // DELETE
    // ==================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá nội dung quyền")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id) {

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
