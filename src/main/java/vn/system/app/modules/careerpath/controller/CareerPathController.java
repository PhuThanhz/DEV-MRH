package vn.system.app.modules.careerpath.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.careerpath.domain.CareerPath;
import vn.system.app.modules.careerpath.domain.request.CareerPathRequest;
import vn.system.app.modules.careerpath.domain.response.CareerPathResponse;
import vn.system.app.modules.careerpath.service.CareerPathService;

@RestController
@RequestMapping("/api/v1/career-paths")
public class CareerPathController {

    private final CareerPathService service;

    public CareerPathController(CareerPathService service) {
        this.service = service;
    }

    @PostMapping
    @ApiMessage("Create career path")
    public ResponseEntity<CareerPathResponse> create(
            @RequestBody CareerPathRequest request) {

        CareerPath entity = service.handleCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.convertToResponse(entity));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update career path")
    public ResponseEntity<CareerPathResponse> update(
            @PathVariable Long id,
            @RequestBody CareerPathRequest request) throws IdInvalidException {

        CareerPath entity = service.handleUpdate(id, request);
        if (entity == null) {
            throw new IdInvalidException("Lộ trình thăng tiến không tồn tại");
        }

        return ResponseEntity.ok(service.convertToResponse(entity));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete career path")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.handleDelete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get career path by id")
    public ResponseEntity<CareerPathResponse> getById(@PathVariable Long id)
            throws IdInvalidException {

        CareerPath entity = service.fetchById(id);
        if (entity == null) {
            throw new IdInvalidException("Lộ trình thăng tiến không tồn tại");
        }

        return ResponseEntity.ok(service.convertToResponse(entity));
    }

    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Get career paths by department")
    public ResponseEntity<List<CareerPathResponse>> getByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }
}
