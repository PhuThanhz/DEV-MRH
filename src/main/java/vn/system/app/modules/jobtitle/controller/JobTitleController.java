package vn.system.app.modules.jobtitle.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.domain.request.ReqCreateJobTitleDTO;
import vn.system.app.modules.jobtitle.domain.request.ReqUpdateJobTitleDTO;
import vn.system.app.modules.jobtitle.domain.response.ResJobTitleDTO;
import vn.system.app.modules.jobtitle.service.JobTitleService;

@RestController
@RequestMapping("/api/v1/job-titles")
@RequiredArgsConstructor
public class JobTitleController {

    private final JobTitleService jobTitleService;

    // =========================
    // CREATE
    // =========================
    @PostMapping
    @ApiMessage("Tạo chức danh mới")
    public ResponseEntity<ResJobTitleDTO> create(
            @Valid @RequestBody ReqCreateJobTitleDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobTitleService.handleCreate(req));
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping
    @ApiMessage("Cập nhật chức danh")
    public ResponseEntity<ResJobTitleDTO> update(
            @Valid @RequestBody ReqUpdateJobTitleDTO req) {

        return ResponseEntity.ok(jobTitleService.handleUpdate(req));
    }

    // =========================
    // DELETE (SOFT DELETE)
    // =========================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá chức danh")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        jobTitleService.handleDelete(id);

        return ResponseEntity.noContent().build(); // ⭐ CHUẨN REST
    }

    // =========================
    // GET ONE
    // =========================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết chức danh")
    public ResponseEntity<ResJobTitleDTO> fetchOne(@PathVariable Long id) {

        return ResponseEntity.ok(jobTitleService.getJobTitle(id));
    }

    // =========================
    // GET ALL
    // =========================
    @GetMapping
    @ApiMessage("Danh sách chức danh")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<JobTitle> spec,
            Pageable pageable) {

        return ResponseEntity.ok(jobTitleService.fetchAll(spec, pageable));
    }
}
