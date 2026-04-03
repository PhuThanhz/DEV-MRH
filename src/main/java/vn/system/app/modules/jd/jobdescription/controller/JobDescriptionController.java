package vn.system.app.modules.jd.jobdescription.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;

import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.jd.jobdescription.domain.request.ReqCreateJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.domain.response.ResJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.service.JobDescriptionService;

@RestController
@RequestMapping("/api/v1")
public class JobDescriptionController {

    private final JobDescriptionService service;

    public JobDescriptionController(JobDescriptionService service) {
        this.service = service;
    }

    @PostMapping("/job-descriptions")
    @ApiMessage("Tạo mới mô tả công việc")
    public ResponseEntity<ResJobDescriptionDTO> create(
            @RequestBody ReqCreateJobDescriptionDTO req) {
        return ResponseEntity.status(201).body(service.convertToDTO(service.handleCreate(req)));
    }

    @PutMapping("/job-descriptions/{id}")
    @ApiMessage("Cập nhật mô tả công việc")
    public ResponseEntity<ResJobDescriptionDTO> update(
            @PathVariable Long id,
            @RequestBody ReqCreateJobDescriptionDTO req) {
        return ResponseEntity.ok(service.convertToDTO(service.handleUpdate(id, req)));
    }

    @DeleteMapping("/job-descriptions/{id}")
    @ApiMessage("Xóa mô tả công việc")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/job-descriptions/{id}")
    @ApiMessage("Lấy chi tiết mô tả công việc")
    public ResponseEntity<ResJobDescriptionDTO> fetchById(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToDTO(service.fetchById(id)));
    }

    @GetMapping("/job-descriptions")
    @ApiMessage("Danh sách mô tả công việc")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<JobDescription> spec,
            Pageable pageable) {
        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    @GetMapping("/job-descriptions/my")
    @ApiMessage("Danh sách JD do tôi tạo")
    public ResponseEntity<ResultPaginationDTO> fetchMyJobDescriptions(Pageable pageable) {
        return ResponseEntity.ok(service.fetchMyJobDescriptions(pageable));
    }

    // ← THÊM 3 ENDPOINT MỚI
    @GetMapping("/job-descriptions/published")
    @ApiMessage("Danh sách JD đã ban hành")
    public ResponseEntity<ResultPaginationDTO> fetchPublished(Pageable pageable) {
        return ResponseEntity.ok(service.fetchPublished(pageable));
    }

    @GetMapping("/job-descriptions/rejected")
    @ApiMessage("Danh sách JD đã từ chối")
    public ResponseEntity<ResultPaginationDTO> fetchRejected(Pageable pageable) {
        return ResponseEntity.ok(service.fetchRejected(pageable));
    }

    @GetMapping("/job-descriptions/all")
    @ApiMessage("Tất cả JD")
    public ResponseEntity<ResultPaginationDTO> fetchAllJd(Pageable pageable) {
        return ResponseEntity.ok(service.fetchAllJd(pageable));
    }
}