package vn.system.app.modules.salarygrade.controller;

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
import vn.system.app.modules.salarygrade.domain.SalaryGrade;
import vn.system.app.modules.salarygrade.domain.request.ReqCreateSalaryGradeDTO;
import vn.system.app.modules.salarygrade.domain.response.ResSalaryGradeDTO;
import vn.system.app.modules.salarygrade.service.SalaryGradeService;

@RestController
@RequestMapping("/api/v1/salary-grades")
@RequiredArgsConstructor
public class SalaryGradeController {

    private final SalaryGradeService salaryGradeService;

    /* ================= CREATE ================= */

    @PostMapping
    @ApiMessage("Tạo bậc lương cho ngữ cảnh chức danh")
    public ResponseEntity<ResSalaryGradeDTO> create(
            @Valid @RequestBody ReqCreateSalaryGradeDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(salaryGradeService.handleCreate(req));
    }

    /* ================= DELETE (SOFT) ================= */

    @DeleteMapping("/{id}")
    @ApiMessage("Xoá bậc lương")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        salaryGradeService.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    /* ================= FETCH ALL ================= */

    @GetMapping
    @ApiMessage("Danh sách bậc lương")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification<SalaryGrade> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                salaryGradeService.fetchAll(spec, pageable));
    }
}
