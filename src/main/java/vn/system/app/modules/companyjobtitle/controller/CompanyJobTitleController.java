package vn.system.app.modules.companyjobtitle.controller;

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
import vn.system.app.modules.companyjobtitle.domain.CompanyJobTitle;
import vn.system.app.modules.companyjobtitle.domain.request.ReqCompanyJobTitleDTO;
import vn.system.app.modules.companyjobtitle.domain.response.ResCompanyJobTitleDTO;
import vn.system.app.modules.companyjobtitle.service.CompanyJobTitleService;

@RestController
@RequestMapping("/api/v1")
public class CompanyJobTitleController {

    private final CompanyJobTitleService service;

    public CompanyJobTitleController(CompanyJobTitleService service) {
        this.service = service;
    }

    /* ================= CREATE ================= */

    @PostMapping("/company-job-titles")
    @ApiMessage("Gán chức danh vào công ty")
    public ResponseEntity<ResCompanyJobTitleDTO> create(
            @Valid @RequestBody ReqCompanyJobTitleDTO req)
            throws IdInvalidException {

        CompanyJobTitle entity = service.handleCreate(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.convertToResDTO(entity));
    }

    /* ================= DELETE (SOFT) ================= */

    @DeleteMapping("/company-job-titles/{id}")
    @ApiMessage("Xoá gán chức danh công ty")
    public ResponseEntity<Void> delete(@PathVariable Long id)
            throws IdInvalidException {

        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    /* ================= GET ONE ================= */

    @GetMapping("/company-job-titles/{id}")
    @ApiMessage("Chi tiết gán chức danh công ty")
    public ResponseEntity<ResCompanyJobTitleDTO> fetchOne(
            @PathVariable Long id)
            throws IdInvalidException {

        CompanyJobTitle entity = service.fetchEntityById(id);
        return ResponseEntity.ok(service.convertToResDTO(entity));
    }

    /* ================= GET ALL ================= */

    @GetMapping("/company-job-titles")
    @ApiMessage("Danh sách gán chức danh công ty")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<CompanyJobTitle> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }
}
