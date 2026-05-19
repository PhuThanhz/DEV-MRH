package vn.system.app.modules.employee.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.employee.domain.request.ReqCreateEmployeeDTO;
import vn.system.app.modules.employee.domain.request.ReqUpdateEmployeeDTO;
import vn.system.app.modules.employee.domain.response.ResCreateEmployeeDTO;
import vn.system.app.modules.employee.domain.response.ResEmployeeDTO;
import vn.system.app.modules.employee.domain.response.ResUpdateEmployeeDTO;
import vn.system.app.modules.employee.service.EmployeeService;
import vn.system.app.modules.user.domain.User;

@RestController
@RequestMapping("/api/v1")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ===========================================================
    // CREATE EMPLOYEE
    // ===========================================================
    @PostMapping("/employees")
    @ApiMessage("Tạo mới nhân viên thành công")
    public ResponseEntity<ResCreateEmployeeDTO> create(
            @Valid @RequestBody ReqCreateEmployeeDTO req) {

        ResCreateEmployeeDTO res = employeeService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ===========================================================
    // UPDATE EMPLOYEE
    // ===========================================================
    @PutMapping("/employees")
    @ApiMessage("Cập nhật nhân viên thành công")
    public ResponseEntity<ResUpdateEmployeeDTO> update(
            @RequestBody ReqUpdateEmployeeDTO req) {

        ResUpdateEmployeeDTO res = employeeService.update(req);
        return ResponseEntity.ok(res);
    }

    // ===========================================================
    // DELETE EMPLOYEE
    // ===========================================================
    @DeleteMapping("/employees/{id}")
    @ApiMessage("Xóa nhân viên thành công")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {

        employeeService.delete(id);
        return ResponseEntity.ok().build();
    }

    // ===========================================================
    // GET EMPLOYEE BY ID
    // ===========================================================
    @GetMapping("/employees/{id}")
    @ApiMessage("Lấy thông tin nhân viên thành công")
    public ResponseEntity<ResEmployeeDTO> getById(
            @PathVariable("id") String id) {

        ResEmployeeDTO res = employeeService.getById(id);
        return ResponseEntity.ok(res);
    }

    // ===========================================================
    // GET ALL EMPLOYEES (PAGINATION + FILTER)
    // ===========================================================
    @GetMapping("/employees")
    @ApiMessage("Lấy danh sách nhân viên thành công")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<User> spec,
            Pageable pageable) {

        return ResponseEntity.ok(employeeService.getAll(spec, pageable));
    }
}