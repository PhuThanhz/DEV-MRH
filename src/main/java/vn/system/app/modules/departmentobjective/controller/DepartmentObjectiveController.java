package vn.system.app.modules.departmentobjective.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.departmentobjective.domain.DepartmentObjective;
import vn.system.app.modules.departmentobjective.domain.request.ReqCreateDepartmentObjective;
import vn.system.app.modules.departmentobjective.domain.response.ResDepartmentObjectiveDTO;
import vn.system.app.modules.departmentobjective.domain.response.ResDepartmentMissionTreeDTO;
import vn.system.app.modules.departmentobjective.service.DepartmentObjectiveService;

@RestController
@RequestMapping("/api/v1")
public class DepartmentObjectiveController {

    private final DepartmentObjectiveService service;

    public DepartmentObjectiveController(DepartmentObjectiveService service) {
        this.service = service;
    }

    /*
     * ==========================================
     * CREATE (OBJECTIVES + TASKS)
     * ==========================================
     * Sau khi tạo xong sẽ trả luôn mission tree
     */
    @PostMapping("/department-objectives")
    @ApiMessage("Tạo mục tiêu và nhiệm vụ phòng ban")
    public ResponseEntity<ResDepartmentMissionTreeDTO> create(
            @Valid @RequestBody ReqCreateDepartmentObjective req)
            throws IdInvalidException {

        service.handleCreate(req);

        ResDepartmentMissionTreeDTO tree = service.fetchMissionTree(req.getDepartmentId());

        return ResponseEntity.status(HttpStatus.CREATED).body(tree);
    }

    /*
     * ==========================================
     * DELETE
     * ==========================================
     */
    @DeleteMapping("/department-objectives/{id}")
    @ApiMessage("Xoá mục tiêu / nhiệm vụ")
    public ResponseEntity<Void> delete(@PathVariable Long id)
            throws IdInvalidException {

        DepartmentObjective e = service.fetchById(id);

        if (e == null) {
            throw new IdInvalidException("Không tìm thấy id = " + id);
        }

        service.handleDelete(id);

        return ResponseEntity.ok().build();
    }

    /*
     * ==========================================
     * FETCH ONE
     * ==========================================
     */
    @GetMapping("/department-objectives/{id}")
    @ApiMessage("Chi tiết mục tiêu / nhiệm vụ")
    public ResponseEntity<ResDepartmentObjectiveDTO> fetchOne(
            @PathVariable Long id)
            throws IdInvalidException {

        DepartmentObjective e = service.fetchById(id);

        if (e == null) {
            throw new IdInvalidException("Không tìm thấy id = " + id);
        }

        return ResponseEntity.ok(service.convert(e));
    }

    /*
     * ==========================================
     * FETCH ALL (pagination + filter)
     * ==========================================
     */
    @GetMapping("/department-objectives")
    @ApiMessage("Danh sách mục tiêu / nhiệm vụ")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<DepartmentObjective> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    /*
     * ==========================================
     * LOAD MISSION TREE BY DEPARTMENT
     * ==========================================
     */
    @GetMapping("/departments/{departmentId}/objectives")
    @ApiMessage("Mục tiêu và nhiệm vụ theo phòng ban")
    public ResponseEntity<ResDepartmentMissionTreeDTO> fetchMissionTree(
            @PathVariable Long departmentId)
            throws IdInvalidException {

        return ResponseEntity.ok(service.fetchMissionTree(departmentId));
    }

}
