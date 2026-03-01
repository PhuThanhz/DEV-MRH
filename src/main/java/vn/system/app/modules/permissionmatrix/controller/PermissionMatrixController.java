package vn.system.app.modules.permissionmatrix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.permissionmatrix.domain.response.FullPermissionMatrixDTO;
import vn.system.app.modules.permissionmatrix.service.PermissionMatrixService;

@RestController
@RequestMapping("/api/v1/permission-categories")
public class PermissionMatrixController {

    private final PermissionMatrixService matrixService;

    public PermissionMatrixController(PermissionMatrixService matrixService) {
        this.matrixService = matrixService;
    }

    @GetMapping("/{categoryId}/matrix")
    @ApiMessage("Matrix phân quyền theo danh mục")
    public ResponseEntity<FullPermissionMatrixDTO> getMatrix(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                matrixService.buildCategoryMatrix(categoryId));
    }
}