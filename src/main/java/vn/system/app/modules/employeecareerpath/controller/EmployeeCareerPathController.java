package vn.system.app.modules.employeecareerpath.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.employeecareerpath.domain.request.ReqAssignCareerPathDTO;
import vn.system.app.modules.employeecareerpath.domain.request.ReqPromoteEmployeeDTO;
import vn.system.app.modules.employeecareerpath.domain.response.ResEmployeeCareerPathDTO;
import vn.system.app.modules.employeecareerpath.domain.response.ResEmployeeCareerPathHistoryDTO;
import vn.system.app.modules.employeecareerpath.service.EmployeeCareerPathService;

@RestController
@RequestMapping("/api/v1/employee-career-paths")
public class EmployeeCareerPathController {

    private final EmployeeCareerPathService service;

    public EmployeeCareerPathController(EmployeeCareerPathService service) {
        this.service = service;
    }

    // POST /api/v1/employee-career-paths
    // HR gán lộ trình cho nhân viên
    @PostMapping
    @ApiMessage("Gán lộ trình thăng tiến cho nhân viên")
    public ResponseEntity<ResEmployeeCareerPathDTO> assign(
            @Valid @RequestBody ReqAssignCareerPathDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.handleAssign(req));
    }

    // PUT /api/v1/employee-career-paths/{id}
    // HR cập nhật note
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật lộ trình thăng tiến")
    public ResponseEntity<ResEmployeeCareerPathDTO> update(
            @PathVariable Long id,
            @RequestBody ReqAssignCareerPathDTO req) {
        return ResponseEntity.ok(service.handleUpdate(id, req));
    }

    // POST /api/v1/employee-career-paths/{id}/promote
    // HR thăng tiến nhân viên lên bước tiếp theo
    @PostMapping("/{id}/promote")
    @ApiMessage("Thăng tiến nhân viên")
    public ResponseEntity<ResEmployeeCareerPathDTO> promote(
            @PathVariable Long id,
            @RequestBody ReqPromoteEmployeeDTO req) {
        return ResponseEntity.ok(service.handlePromote(id, req));
    }

    // PATCH /api/v1/employee-career-paths/{id}/status
    // HR đổi trạng thái: 0=in_progress, 2=on_hold
    @PatchMapping("/{id}/status")
    @ApiMessage("Cập nhật trạng thái lộ trình")
    public ResponseEntity<Void> setStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        service.handleSetStatus(id, status);
        return ResponseEntity.ok().build();
    }

    // PATCH /api/v1/employee-career-paths/{id}/deactivate
    // Kết thúc lộ trình (nhân viên nghỉ việc, chuyển phòng...)
    @PatchMapping("/{id}/deactivate")
    @ApiMessage("Kết thúc lộ trình thăng tiến")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.handleDeactivate(id);
        return ResponseEntity.ok().build();
    }

    // GET /api/v1/employee-career-paths/user/{userId}
    // Xem lộ trình hiện tại của 1 nhân viên
    @GetMapping("/user/{userId}")
    @ApiMessage("Lấy lộ trình thăng tiến của nhân viên")
    public ResponseEntity<ResEmployeeCareerPathDTO> getByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(service.fetchByUserId(userId));
    }

    // GET /api/v1/employee-career-paths/department/{departmentId}
    // HR xem lộ trình toàn phòng ban
    @GetMapping("/department/{departmentId}")
    @ApiMessage("Lấy lộ trình thăng tiến theo phòng ban")
    public ResponseEntity<List<ResEmployeeCareerPathDTO>> getByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    // GET /api/v1/employee-career-paths/upcoming-promotions
    // Danh sách nhân viên sắp đến hạn thăng tiến
    @GetMapping("/upcoming-promotions")
    @ApiMessage("Danh sách nhân viên sắp đến hạn thăng tiến")
    public ResponseEntity<List<ResEmployeeCareerPathDTO>> getUpcoming(
            @RequestParam(defaultValue = "30") int withinDays) {
        return ResponseEntity.ok(service.fetchUpcomingPromotions(withinDays));
    }

    // GET /api/v1/employee-career-paths/history/{userId}
    // Lịch sử thăng tiến của nhân viên
    @GetMapping("/history/{userId}")
    @ApiMessage("Lịch sử thăng tiến của nhân viên")
    public ResponseEntity<List<ResEmployeeCareerPathHistoryDTO>> getHistory(
            @PathVariable String userId) {
        return ResponseEntity.ok(service.fetchHistory(userId));
    }
}