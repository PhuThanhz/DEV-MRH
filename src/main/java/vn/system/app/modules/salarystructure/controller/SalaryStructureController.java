package vn.system.app.modules.salarystructure.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;

import vn.system.app.modules.salarystructure.domain.SalaryStructure;
import vn.system.app.modules.salarystructure.domain.request.ReqUpsertSalaryStructureDTO;
import vn.system.app.modules.salarystructure.service.SalaryStructureService;

@RestController
@RequestMapping("/api/v1/salary-structures")
@RequiredArgsConstructor
public class SalaryStructureController {

    private final SalaryStructureService service;

    @PostMapping("/upsert")
    @ApiMessage("Tạo hoặc cập nhật cấu trúc lương")
    public ResponseEntity<?> upsert(@Valid @RequestBody ReqUpsertSalaryStructureDTO req) {
        return ResponseEntity.ok(service.upsert(req));
    }

    @GetMapping
    @ApiMessage("Danh sách cấu trúc lương (pagination)")
    public ResponseEntity<ResultPaginationDTO> list(
            @Filter Specification<SalaryStructure> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    @GetMapping("/{id}")
    @ApiMessage("Chi tiết cấu trúc lương")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
