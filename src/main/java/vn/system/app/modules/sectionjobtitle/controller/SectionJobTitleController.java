package vn.system.app.modules.sectionjobtitle.controller;

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
import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;
import vn.system.app.modules.sectionjobtitle.domain.request.ReqSectionJobTitleDTO;
import vn.system.app.modules.sectionjobtitle.domain.response.ResSectionJobTitleDTO;
import vn.system.app.modules.sectionjobtitle.service.SectionJobTitleService;

@RestController
@RequestMapping("/api/v1")
public class SectionJobTitleController {

    private final SectionJobTitleService service;

    public SectionJobTitleController(SectionJobTitleService service) {
        this.service = service;
    }

    /* ================= CREATE ================= */

    @PostMapping("/section-job-titles")
    @ApiMessage("Gán chức danh vào bộ phận")
    public ResponseEntity<ResSectionJobTitleDTO> create(
            @Valid @RequestBody ReqSectionJobTitleDTO req)
            throws IdInvalidException {

        SectionJobTitle entity = service.handleCreate(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.convertToResDTO(entity));
    }

    /* ================= DELETE (SOFT) ================= */

    @DeleteMapping("/section-job-titles/{id}")
    @ApiMessage("Xoá gán chức danh bộ phận")
    public ResponseEntity<Void> delete(@PathVariable Long id)
            throws IdInvalidException {

        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    /* ================= GET ONE ================= */

    @GetMapping("/section-job-titles/{id}")
    @ApiMessage("Chi tiết gán chức danh bộ phận")
    public ResponseEntity<ResSectionJobTitleDTO> fetchOne(
            @PathVariable Long id)
            throws IdInvalidException {

        SectionJobTitle entity = service.fetchEntityById(id);
        return ResponseEntity.ok(service.convertToResDTO(entity));
    }

    /* ================= GET ALL ================= */

    @GetMapping("/section-job-titles")
    @ApiMessage("Danh sách gán chức danh bộ phận")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<SectionJobTitle> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }
}
