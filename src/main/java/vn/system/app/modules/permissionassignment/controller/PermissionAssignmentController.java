package vn.system.app.modules.permissionassignment.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.permissionassignment.domain.request.ReqAssignPermissionDTO;
import vn.system.app.modules.permissionassignment.domain.response.ResPermissionMatrixDTO;
import vn.system.app.modules.permissionassignment.service.PermissionAssignmentService;

@RestController
@RequestMapping("/api/v1/permission-contents")
public class PermissionAssignmentController {

    private final PermissionAssignmentService service;

    public PermissionAssignmentController(PermissionAssignmentService service) {
        this.service = service;
    }

    /*
     * =====================================================
     * GET MATRIX BY CONTENT ID
     * =====================================================
     */
    @GetMapping("/{contentId}/matrix")
    @ApiMessage("Chi tiết phân quyền theo nội dung")
    public ResponseEntity<ResPermissionMatrixDTO> getMatrix(@PathVariable Long contentId) {
        return ResponseEntity.ok(service.buildMatrix(contentId));
    }

    /*
     * =====================================================
     * ASSIGN PERMISSION
     * =====================================================
     */
    @PostMapping("/{contentId}/assign")
    @ApiMessage("Gán quyền cho chức danh")
    public ResponseEntity<Void> assign(
            @PathVariable Long contentId,
            @Valid @RequestBody ReqAssignPermissionDTO req) {

        service.assign(contentId, req);
        return ResponseEntity.ok().build();
    }
}
