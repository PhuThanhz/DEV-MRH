package vn.system.app.modules.jd.jobdescription.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.jd.jobdescription.domain.request.ReqCreateFullJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.domain.request.ReqUpdateFullJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.domain.response.ResFullJobDescriptionDTO;
import vn.system.app.modules.jd.jobdescription.service.JobDescriptionService;

@RestController
@RequestMapping("/api/v1/job-descriptions")
public class JobDescriptionController {

        private final JobDescriptionService service;

        public JobDescriptionController(JobDescriptionService service) {
                this.service = service;
        }

        // ================= CREATE =================
        @PostMapping
        @ApiMessage("Tạo Job Description")
        public ResponseEntity<ResFullJobDescriptionDTO> create(
                        @Valid @RequestBody ReqCreateFullJobDescriptionDTO req) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(service.handleCreate(req));
        }

        // ================= UPDATE =================
        @PutMapping
        @ApiMessage("Cập nhật Job Description")
        public ResponseEntity<ResFullJobDescriptionDTO> update(
                        @Valid @RequestBody ReqUpdateFullJobDescriptionDTO req) {

                return ResponseEntity.ok(service.handleUpdate(req));
        }

        // ================= FETCH ONE =================
        @GetMapping("/{id}")
        @ApiMessage("Chi tiết Job Description")
        public ResponseEntity<ResFullJobDescriptionDTO> fetchOne(
                        @PathVariable Long id) {

                return ResponseEntity.ok(service.fetchFullById(id));
        }

        // ================= FETCH ALL =================
        @GetMapping
        @ApiMessage("Danh sách Job Description")
        public ResponseEntity<ResultPaginationDTO> fetchAll(
                        @RequestParam(name = "search", required = false) String search,
                        Pageable pageable) {

                return ResponseEntity.ok(service.fetchAll(search, pageable));
        }
}