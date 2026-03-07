package vn.system.app.modules.departmentprocedure.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;
import vn.system.app.modules.departmentprocedure.domain.request.ReqCreateDepartmentProcedure;
import vn.system.app.modules.departmentprocedure.domain.response.ResDepartmentProcedureDTO;
import vn.system.app.modules.departmentprocedure.service.DepartmentProcedureService;

@RestController
@RequestMapping("/api/v1")
public class DepartmentProcedureController {

    private final DepartmentProcedureService service;

    public DepartmentProcedureController(DepartmentProcedureService service) {
        this.service = service;
    }

    /*
     * CREATE
     */
    @PostMapping("/department-procedures")
    @ApiMessage("Create department procedure")
    public ResponseEntity<ResDepartmentProcedureDTO> create(
            @RequestBody ReqCreateDepartmentProcedure req) throws IdInvalidException {

        DepartmentProcedure created = service.handleCreate(req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.convertToDTO(created));
    }

    /*
     * DELETE
     */
    @DeleteMapping("/department-procedures/{id}")
    @ApiMessage("Delete department procedure")
    public ResponseEntity<Void> delete(@PathVariable Long id)
            throws IdInvalidException {

        service.handleDelete(id);

        return ResponseEntity.ok().build();
    }

    /*
     * GET BY ID
     */
    @GetMapping("/department-procedures/{id}")
    @ApiMessage("Get department procedure by id")
    public ResponseEntity<ResDepartmentProcedureDTO> getById(@PathVariable Long id)
            throws IdInvalidException {

        DepartmentProcedure p = service.fetchById(id);

        if (p == null) {
            throw new IdInvalidException("Procedure không tồn tại");
        }

        return ResponseEntity.ok(service.convertToDTO(p));
    }

    /*
     * GET ALL
     */
    @GetMapping("/department-procedures")
    @ApiMessage("Fetch all department procedures")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<DepartmentProcedure> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    /*
     * UPDATE
     */
    @PutMapping("/department-procedures/{id}")
    @ApiMessage("Update department procedure")
    public ResponseEntity<ResDepartmentProcedureDTO> update(
            @PathVariable Long id,
            @RequestBody ReqCreateDepartmentProcedure req)
            throws IdInvalidException {

        DepartmentProcedure updated = service.handleUpdate(id, req);

        return ResponseEntity.ok(service.convertToDTO(updated));
    }
}