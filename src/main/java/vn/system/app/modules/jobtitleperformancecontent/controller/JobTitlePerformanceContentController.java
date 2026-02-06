package vn.system.app.modules.jobtitleperformancecontent.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;

import vn.system.app.modules.jobtitleperformancecontent.domain.JobTitlePerformanceContent;
import vn.system.app.modules.jobtitleperformancecontent.domain.OwnerLevel;
import vn.system.app.modules.jobtitleperformancecontent.domain.request.ReqCreateOrUpdateJobTitlePerformanceContentDTO;
import vn.system.app.modules.jobtitleperformancecontent.service.JobTitlePerformanceContentService;

@RestController
@RequestMapping("/api/v1/job-title-performance-contents")
public class JobTitlePerformanceContentController {

    private final JobTitlePerformanceContentService service;

    public JobTitlePerformanceContentController(JobTitlePerformanceContentService service) {
        this.service = service;
    }

    @PostMapping
    @ApiMessage("Tạo nội dung đánh giá")
    public ResponseEntity<?> create(
            @Valid @RequestBody ReqCreateOrUpdateJobTitlePerformanceContentDTO req) {

        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật nội dung đánh giá")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody ReqCreateOrUpdateJobTitlePerformanceContentDTO req) {

        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Vô hiệu nội dung đánh giá")
    public ResponseEntity<?> disable(@PathVariable Long id) {
        service.disable(id);
        return ResponseEntity.ok("Đã vô hiệu");
    }

    @PutMapping("/{id}/restore")
    @ApiMessage("Khôi phục nội dung đánh giá")
    public ResponseEntity<?> restore(@PathVariable Long id) {
        service.restore(id);
        return ResponseEntity.ok("Đã khôi phục");
    }

    @GetMapping
    @ApiMessage("Danh sách nội dung đánh giá (pagination)")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<JobTitlePerformanceContent> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    @GetMapping("/by-owner")
    @ApiMessage("Danh sách nội dung theo OwnerLevel + JobTitleId")
    public ResponseEntity<?> getByOwner(
            @RequestParam OwnerLevel ownerLevel,
            @RequestParam Long ownerJobTitleId) {

        return ResponseEntity.ok(service.findByOwner(ownerLevel, ownerJobTitleId));
    }
}
