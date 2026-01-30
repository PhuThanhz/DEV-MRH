package vn.system.app.modules.jd.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.domain.JobDescription;
import vn.system.app.modules.jd.domain.request.ReqCreateJobDescription;
import vn.system.app.modules.jd.domain.request.ReqUpdateJobDescription;
import vn.system.app.modules.jd.domain.response.ResJobDescriptionDTO;
import vn.system.app.modules.jd.service.JobDescriptionService;

@RestController
@RequestMapping("/api/v1/job-descriptions")
public class JobDescriptionController {

    private final JobDescriptionService service;

    public JobDescriptionController(JobDescriptionService service) {
        this.service = service;
    }
    /* ================= CREATE ================= */

    @PostMapping
    @ApiMessage("Create Job Description")
    public ResponseEntity<ResJobDescriptionDTO> create(
            @Valid @RequestBody ReqCreateJobDescription req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        service.toRes(
                                service.create(req)));
    }

    /* ================= UPDATE ================= */
    @PutMapping
    @ApiMessage("Update Job Description")
    public ResponseEntity<ResJobDescriptionDTO> update(
            @Valid @RequestBody ReqUpdateJobDescription req)
            throws IdInvalidException {

        return ResponseEntity.ok(
                service.toRes(
                        service.update(req)));
    }
    /* ================= ISSUE ================= */

    @PostMapping("/{id}/issue")
    @ApiMessage("Issue Job Description")
    public ResponseEntity<ResJobDescriptionDTO> issue(
            @PathVariable Long id)
            throws IdInvalidException {

        return ResponseEntity.ok(
                service.toRes(
                        service.issue(id)));
    }

    /* ================= GET BY ID ================= */
    @GetMapping("/{id}")
    @ApiMessage("Get Job Description by ID")
    public ResponseEntity<ResJobDescriptionDTO> getById(
            @PathVariable Long id)
            throws IdInvalidException {

        return ResponseEntity.ok(
                service.toRes(
                        service.fetchById(id)));
    }
    /* ================= LIST ================= */

    @GetMapping
    @ApiMessage("Fetch all Job Descriptions")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<JobDescription> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                service.fetchAll(spec, pageable));
    }
}
