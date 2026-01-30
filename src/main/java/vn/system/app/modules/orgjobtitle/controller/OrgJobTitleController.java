package vn.system.app.modules.orgjobtitle.controller;

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
import vn.system.app.modules.orgjobtitle.domain.OrgJobTitle;
import vn.system.app.modules.orgjobtitle.domain.request.ReqCreateOrgJobTitleDTO;
import vn.system.app.modules.orgjobtitle.domain.response.ResOrgJobTitleDTO;
import vn.system.app.modules.orgjobtitle.service.OrgJobTitleService;

@RestController
@RequestMapping("/api/v1")
public class OrgJobTitleController {

    private final OrgJobTitleService orgJobTitleService;

    public OrgJobTitleController(OrgJobTitleService orgJobTitleService) {
        this.orgJobTitleService = orgJobTitleService;
    }

    /* ================= CREATE ================= */

    @PostMapping("/org-job-titles")
    @ApiMessage("Gán chức danh vào đơn vị (công ty / phòng ban / bộ phận)")
    public ResponseEntity<ResOrgJobTitleDTO> create(
            @Valid @RequestBody ReqCreateOrgJobTitleDTO req)
            throws IdInvalidException {

        OrgJobTitle entity = orgJobTitleService.handleCreate(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orgJobTitleService.convertToResDTO(entity));
    }

    /* ================= DELETE (SOFT) ================= */

    @DeleteMapping("/org-job-titles/{id}")
    @ApiMessage("Xoá gán chức danh")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        OrgJobTitle entity = orgJobTitleService.fetchEntityById(id);
        if (entity == null) {
            throw new IdInvalidException(
                    "OrgJobTitle với id = " + id + " không tồn tại");
        }

        orgJobTitleService.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    /* ================= GET ONE ================= */

    @GetMapping("/org-job-titles/{id}")
    @ApiMessage("Chi tiết gán chức danh")
    public ResponseEntity<ResOrgJobTitleDTO> fetchOne(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        OrgJobTitle entity = orgJobTitleService.fetchEntityById(id);
        if (entity == null) {
            throw new IdInvalidException(
                    "OrgJobTitle với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(
                orgJobTitleService.convertToResDTO(entity));
    }

    /* ================= GET ALL ================= */

    @GetMapping("/org-job-titles")
    @ApiMessage("Danh sách gán chức danh")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<OrgJobTitle> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                orgJobTitleService.fetchAll(spec, pageable));
    }
}
