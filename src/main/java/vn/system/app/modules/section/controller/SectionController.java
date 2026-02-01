package vn.system.app.modules.section.controller;

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
import vn.system.app.modules.section.domain.request.ReqCreateSectionDTO;
import vn.system.app.modules.section.domain.request.ReqUpdateSectionDTO;
import vn.system.app.modules.section.domain.response.ResSectionDTO;
import vn.system.app.modules.section.service.SectionService;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    /* ================= CREATE ================= */

    @PostMapping
    @ApiMessage("Tạo bộ phận mới")
    public ResponseEntity<ResSectionDTO> create(@Valid @RequestBody ReqCreateSectionDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sectionService.createSection(req));
    }

    /* ================= UPDATE ================= */

    @PutMapping
    @ApiMessage("Cập nhật bộ phận")
    public ResponseEntity<ResSectionDTO> update(@Valid @RequestBody ReqUpdateSectionDTO req) {
        return ResponseEntity.ok(sectionService.updateSection(req));
    }

    /* ================= INACTIVE (TẮT BỘ PHẬN) ================= */

    @PutMapping("/{id}/inactive")
    @ApiMessage("Vô hiệu hoá bộ phận")
    public ResponseEntity<String> inactive(@PathVariable Long id) {
        sectionService.setInactive(id);
        return ResponseEntity.ok("Đã vô hiệu hoá bộ phận ID = " + id);
    }

    /* ================= ACTIVE (BẬT LẠI BỘ PHẬN) ================= */

    @PutMapping("/{id}/active")
    @ApiMessage("Kích hoạt lại bộ phận")
    public ResponseEntity<String> active(@PathVariable Long id) {
        sectionService.setActive(id);
        return ResponseEntity.ok("Đã kích hoạt bộ phận ID = " + id);
    }

    /* ================= FETCH ONE ================= */

    @GetMapping("/{id}")
    @ApiMessage("Chi tiết bộ phận")
    public ResponseEntity<ResSectionDTO> fetchOne(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.fetchOne(id));
    }

    /* ================= FETCH ALL ================= */

    @GetMapping
    @ApiMessage("Danh sách bộ phận")
    public ResponseEntity<ResultPaginationDTO> fetchAll(
            @Filter Specification spec,
            Pageable pageable) {

        return ResponseEntity.ok(sectionService.fetchAll(spec, pageable));
    }
}
