package vn.system.app.modules.deptmission.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.deptmission.domain.request.ReqDeptMissionDTO;
import vn.system.app.modules.deptmission.domain.response.ResDeptMissionDTO;
import vn.system.app.modules.deptmission.service.DeptMissionService;

@RestController
@RequestMapping("/api/v1/dept-missions")
@RequiredArgsConstructor
public class DeptMissionController {

    private final DeptMissionService service;

    @PostMapping
    @ApiMessage("Tạo mục tiêu & nhiệm vụ phòng ban")
    public ResponseEntity<ResDeptMissionDTO> create(
            @Valid @RequestBody ReqDeptMissionDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(req));
    }

    @PutMapping
    @ApiMessage("Cập nhật mục tiêu & nhiệm vụ phòng ban")
    public ResponseEntity<ResDeptMissionDTO> update(
            @Valid @RequestBody ReqDeptMissionDTO req) {

        return ResponseEntity.ok(service.update(req));
    }

    @GetMapping("/{departmentId}")
    @ApiMessage("Lấy mục tiêu & nhiệm vụ theo phòng ban")
    public ResponseEntity<ResDeptMissionDTO> fetch(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(
                service.fetchByDepartmentId(departmentId));
    }

    @DeleteMapping("/{departmentId}")
    @ApiMessage("Xoá toàn bộ nhiệm vụ phòng ban")
    public ResponseEntity<Void> delete(
            @PathVariable Long departmentId) {

        service.deleteByDepartment(departmentId);
        return ResponseEntity.ok().build();
    }
}
