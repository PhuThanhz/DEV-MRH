package vn.system.app.modules.departmentjobtitle.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.domain.request.ReqDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.domain.response.ResDepartmentJobTitleDTO;
import vn.system.app.modules.departmentjobtitle.service.DepartmentJobTitleService;

@RestController
@RequestMapping("/api/v1")
public class DepartmentJobTitleController {

    private final DepartmentJobTitleService service;

    public DepartmentJobTitleController(DepartmentJobTitleService service) {
        this.service = service;
    }

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @PostMapping("/department-job-titles")
    @ApiMessage("Gán chức danh vào phòng ban")
    public ResponseEntity<ResDepartmentJobTitleDTO> create(
            @Valid @RequestBody ReqDepartmentJobTitleDTO req)
            throws IdInvalidException {

        DepartmentJobTitle entity = this.service.handleCreate(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.service.convertToResDTO(entity));
    }

    /*
     * =====================================================
     * SOFT DELETE
     * =====================================================
     */
    @DeleteMapping("/department-job-titles/{id}")
    @ApiMessage("Huỷ gán chức danh phòng ban")
    public ResponseEntity<Void> delete(@PathVariable Long id)
            throws IdInvalidException {

        this.service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    /*
     * =====================================================
     * RESTORE
     * =====================================================
     */
    @PatchMapping("/department-job-titles/{id}/restore")
    @ApiMessage("Kích hoạt lại chức danh phòng ban")
    public ResponseEntity<ResDepartmentJobTitleDTO> restore(@PathVariable Long id)
            throws IdInvalidException {

        DepartmentJobTitle entity = this.service.restore(id);
        return ResponseEntity.ok(this.service.convertToResDTO(entity));
    }

    /*
     * =====================================================
     * GET ONE
     * =====================================================
     */
    @GetMapping("/department-job-titles/{id}")
    @ApiMessage("Chi tiết gán chức danh phòng ban")
    public ResponseEntity<ResDepartmentJobTitleDTO> fetchOne(@PathVariable Long id)
            throws IdInvalidException {

        DepartmentJobTitle entity = this.service.fetchEntityById(id);
        return ResponseEntity.ok(this.service.convertToResDTO(entity));
    }

    /*
     * =====================================================
     * GET ALL (PAGINATION)
     * =====================================================
     */
    @GetMapping("/department-job-titles")
    @ApiMessage("Danh sách gán chức danh phòng ban")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<DepartmentJobTitle> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.service.fetchAll(spec, pageable));
    }

    /*
     * =====================================================
     * GET JOB TITLES BY DEPARTMENT
     * =====================================================
     */
    @GetMapping("/departments/{departmentId}/job-titles")
    @ApiMessage("Danh sách chức danh đang hoạt động trong phòng ban")
    public ResponseEntity<List<ResDepartmentJobTitleDTO>> fetchJobTitlesByDepartment(
            @PathVariable("departmentId") Long departmentId)
            throws IdInvalidException {

        List<DepartmentJobTitle> list = this.service.fetchByDepartment(departmentId);

        return ResponseEntity.ok(
                list.stream().map(this.service::convertToResDTO).collect(Collectors.toList()));
    }

    /*
     * =====================================================
     * ❌ REMOVE — Endpoint này TRÙNG với CareerPathController
     * GET CAREER PATH BY BAND
     * =====================================================
     */

    // @GetMapping("/departments/{departmentId}/career-paths/by-band")
    // @ApiMessage("Lộ trình thăng tiến theo từng cấp (band riêng)")
    // public ResponseEntity<Map<String, List<ResDepartmentJobTitleDTO>>>
    // fetchCareerPathByBand(
    // @PathVariable("departmentId") Long departmentId)
    // throws IdInvalidException {
    //
    // return ResponseEntity.ok(this.service.fetchCareerPathByBand(departmentId));
    // }

    /*
     * =====================================================
     * ❌ REMOVE — Endpoint này TRÙNG với CareerPathController
     * GET GLOBAL CAREER PATH
     * =====================================================
     */

    // @GetMapping("/departments/{departmentId}/career-paths/global")
    // @ApiMessage("Lộ trình thăng tiến liên cấp")
    // public ResponseEntity<List<ResDepartmentJobTitleDTO>> fetchGlobalCareerPath(
    // @PathVariable("departmentId") Long departmentId)
    // throws IdInvalidException {
    //
    // return ResponseEntity.ok(this.service.fetchGlobalCareerPath(departmentId));
    // }
}
