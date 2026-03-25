package vn.system.app.modules.jd.jobdescription.controller;

import java.util.List;

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

    /*
     * ==========================================
     * TẠO JD
     * ==========================================
     */
    @PostMapping("/job-descriptions")
    @ApiMessage("Tạo mới mô tả công việc")
    public ResponseEntity<ResJobDescriptionDTO> create(
            @RequestBody ReqCreateJobDescriptionDTO req) {

        JobDescription created = service.handleCreate(req);

        return ResponseEntity
                .status(201)
                .body(service.convertToDTO(created));
    }

    /*
     * ==========================================
     * CẬP NHẬT JD
     * ==========================================
     */
    @PutMapping("/job-descriptions/{id}")
    @ApiMessage("Cập nhật mô tả công việc")
    public ResponseEntity<ResJobDescriptionDTO> update(
            @PathVariable Long id,
            @RequestBody ReqCreateJobDescriptionDTO req) {

        JobDescription updated = service.handleUpdate(id, req);

        return ResponseEntity.ok(service.convertToDTO(updated));
    }

    /*
     * ==========================================
     * XÓA JD
     * ==========================================
     */
    @DeleteMapping("/job-descriptions/{id}")
    @ApiMessage("Xóa mô tả công việc")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.handleDelete(id);

        return ResponseEntity.ok().build();
    }

    /*
     * ==========================================
     * LẤY JD THEO ID
     * ==========================================
     */
    @GetMapping("/job-descriptions/{id}")
    @ApiMessage("Lấy chi tiết mô tả công việc")
    public ResponseEntity<ResJobDescriptionDTO> fetchById(@PathVariable Long id) {

        JobDescription jd = service.fetchById(id);

        return ResponseEntity.ok(service.convertToDTO(jd));
    }

    /*
     * ==========================================
     * DANH SÁCH JD
     * ==========================================
     */
    @GetMapping("/job-descriptions")
    @ApiMessage("Danh sách mô tả công việc")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<JobDescription> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    /*
     * ==========================================
     * JD TÔI TẠO
     * ==========================================
     */
    @GetMapping("/job-descriptions/my")
    @ApiMessage("Danh sách JD do tôi tạo")
    public ResponseEntity<ResultPaginationDTO> fetchMyJobDescriptions(Pageable pageable) {
        return ResponseEntity.ok(service.fetchMyJobDescriptions(pageable));
    }

}