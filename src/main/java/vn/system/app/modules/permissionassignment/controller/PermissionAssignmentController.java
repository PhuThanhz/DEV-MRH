package vn.system.app.modules.permissionassignment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.permissionassignment.domain.request.ReqPermissionAssignmentDTO;
import vn.system.app.modules.permissionassignment.domain.response.ResPermissionAssignmentDTO;
import vn.system.app.modules.permissionassignment.service.PermissionAssignmentService;

@RestController
@RequestMapping("/api/v1/permission-assignments")
public class PermissionAssignmentController {

    private final PermissionAssignmentService service;

    public PermissionAssignmentController(PermissionAssignmentService service) {
        this.service = service;
    }

    // ================= CREATE =================
    @PostMapping
    @ApiMessage("Gán quyền cho chức danh")
    public ResponseEntity<ResPermissionAssignmentDTO> create(
            @Valid @RequestBody ReqPermissionAssignmentDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(req));
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật quyền cho chức danh")
    public ResponseEntity<ResPermissionAssignmentDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReqPermissionAssignmentDTO req) {

        return ResponseEntity.ok(
                service.update(id, req));
    }

    // ================= GET ONE =================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết gán quyền")
    public ResponseEntity<ResPermissionAssignmentDTO> getOne(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.fetchDetail(id));
    }

    // ================= GET BY CONTENT =================
    @GetMapping
    @ApiMessage("Danh sách quyền theo nội dung")
    public ResponseEntity<?> getByContent(
            @RequestParam Long permissionContentId) {

        return ResponseEntity.ok(
                service.fetchByPermissionContent(permissionContentId));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá gán quyền")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
