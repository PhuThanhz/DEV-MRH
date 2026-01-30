package vn.system.app.modules.departmentjobtitle.controller;

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

    /* ================= CREATE ================= */

    @PostMapping("/department-job-titles")
    @ApiMessage("Gán chức danh vào phòng ban")
    public ResponseEntity<ResDepartmentJobTitleDTO> create(
            @Valid @RequestBody ReqDepartmentJobTitleDTO req)
            throws IdInvalidException {

        DepartmentJobTitle entity = service.handleCreate(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.convertToResDTO(entity));
    }

    /* ================= DELETE (SOFT) ================= */

    @DeleteMapping("/department-job-titles/{id}")
    @ApiMessage("Xoá gán chức danh phòng ban")
    public ResponseEntity<Void> delete(@PathVariable Long id)
            throws IdInvalidException {

        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    /* ================= GET ONE ================= */

    @GetMapping("/department-job-titles/{id}")
    @ApiMessage("Chi tiết gán chức danh phòng ban")
    public ResponseEntity<ResDepartmentJobTitleDTO> fetchOne(
            @PathVariable Long id)
            throws IdInvalidException {

        DepartmentJobTitle entity = service.fetchEntityById(id);
        return ResponseEntity.ok(service.convertToResDTO(entity));
    }

    /* ================= GET ALL ================= */

    @GetMapping("/department-job-titles")
    @ApiMessage("Danh sách gán chức danh phòng ban")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<DepartmentJobTitle> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }
}
