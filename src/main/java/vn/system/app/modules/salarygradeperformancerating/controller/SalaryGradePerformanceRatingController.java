package vn.system.app.modules.salarygradeperformancerating.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.salarygradeperformancerating.domain.SalaryGradePerformanceRating;
import vn.system.app.modules.salarygradeperformancerating.domain.request.SalaryGradePerformanceRatingRequest;
import vn.system.app.modules.salarygradeperformancerating.domain.response.SalaryGradePerformanceRatingResponse;
import vn.system.app.modules.salarygradeperformancerating.service.SalaryGradePerformanceRatingService;

@RestController
@RequestMapping("/api/v1")
public class SalaryGradePerformanceRatingController {

    private final SalaryGradePerformanceRatingService service;

    public SalaryGradePerformanceRatingController(
            SalaryGradePerformanceRatingService service) {
        this.service = service;
    }

    @PostMapping("/salary-grade-performance-ratings")
    @ApiMessage("Create salary grade performance rating")
    public ResponseEntity<SalaryGradePerformanceRatingResponse> create(
            @RequestBody SalaryGradePerformanceRatingRequest request)
            throws IdInvalidException {

        SalaryGradePerformanceRating entity = this.service.handleCreate(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.service.convertToResponse(entity));
    }

    @DeleteMapping("/salary-grade-performance-ratings/{id}")
    @ApiMessage("Delete salary grade performance rating")
    public ResponseEntity<Void> delete(
            @PathVariable("id") long id)
            throws IdInvalidException {

        SalaryGradePerformanceRating current = this.service.fetchById(id);

        if (current == null) {
            throw new IdInvalidException(
                    "Xếp loại thực hiện hiệu quả công việc với id = "
                            + id + " không tồn tại");
        }

        this.service.handleDeleteById(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/salary-grade-performance-ratings/{id}")
    @ApiMessage("Fetch salary grade performance rating by id")
    public ResponseEntity<SalaryGradePerformanceRatingResponse> getById(
            @PathVariable("id") long id)
            throws IdInvalidException {

        SalaryGradePerformanceRating entity = this.service.fetchById(id);

        if (entity == null) {
            throw new IdInvalidException(
                    "Xếp loại thực hiện hiệu quả công việc với id = "
                            + id + " không tồn tại");
        }

        return ResponseEntity.ok(
                this.service.convertToResponse(entity));
    }

    @GetMapping("/salary-grade-performance-ratings/by-salary-grade/{salaryGradeId}")
    @ApiMessage("Fetch salary grade performance rating by salary grade")
    public ResponseEntity<SalaryGradePerformanceRatingResponse> getBySalaryGrade(
            @PathVariable("salaryGradeId") long salaryGradeId)
            throws IdInvalidException {

        SalaryGradePerformanceRating entity = this.service.fetchBySalaryGrade(salaryGradeId);

        if (entity == null) {
            throw new IdInvalidException(
                    "Chưa có xếp loại thực hiện hiệu quả công việc cho bậc lương này");
        }

        return ResponseEntity.ok(
                this.service.convertToResponse(entity));
    }

    @GetMapping("/salary-grade-performance-ratings")
    @ApiMessage("Fetch all salary grade performance ratings")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<SalaryGradePerformanceRating> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                this.service.fetchAll(spec, pageable));
    }

    @PutMapping("/salary-grade-performance-ratings/{id}")
    @ApiMessage("Update salary grade performance rating")
    public ResponseEntity<SalaryGradePerformanceRatingResponse> update(
            @PathVariable("id") long id,
            @RequestBody SalaryGradePerformanceRatingRequest request)
            throws IdInvalidException {

        SalaryGradePerformanceRating entity = this.service.handleUpdate(request, id);

        if (entity == null) {
            throw new IdInvalidException(
                    "Xếp loại thực hiện hiệu quả công việc với id = "
                            + id + " không tồn tại");
        }

        return ResponseEntity.ok(
                this.service.convertToResponse(entity));
    }
}