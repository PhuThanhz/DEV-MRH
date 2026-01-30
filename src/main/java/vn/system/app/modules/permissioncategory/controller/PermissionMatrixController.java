package vn.system.app.modules.permissioncategory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.permissioncategory.domain.response.PermissionCategoryMatrixResponse;
import vn.system.app.modules.permissioncategory.service.PermissionMatrixService;

@RestController
@RequestMapping("/api/v1/permission-categories")
public class PermissionMatrixController {

    private final PermissionMatrixService service;

    public PermissionMatrixController(PermissionMatrixService service) {
        this.service = service;
    }

    // ==================================================
    // GET MATRIX – BẢNG PHÂN QUYỀN THEO DANH MỤC
    // ==================================================
    @GetMapping("/{categoryId}/matrix")
    @ApiMessage("Bảng phân quyền theo danh mục")
    public ResponseEntity<PermissionCategoryMatrixResponse> getMatrix(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                service.buildMatrix(categoryId));
    }
}
