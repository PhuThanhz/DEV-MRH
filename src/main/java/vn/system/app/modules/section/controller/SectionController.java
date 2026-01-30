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
import vn.system.app.modules.section.domain.Section;
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
    public ResponseEntity<ResSectionDTO> create(
            @Valid @RequestBody ReqCreateSectionDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(sectionService.createSection(req));
    }

    /* ================= UPDATE ================= */

    @PutMapping
    @ApiMessage("Cập nhật bộ phận")
    public ResponseEntity<ResSectionDTO> update(
            @Valid @RequestBody ReqUpdateSectionDTO req) {

        return ResponseEntity.ok(sectionService.updateSection(req));
    }

    /* ================= DELETE (SOFT) ================= */

    @DeleteMapping("/{id}")
    @ApiMessage("Xoá bộ phận")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sectionService.handleDelete(id);
        return ResponseEntity.ok().build();
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
            @Filter Specification<Section> spec,
            Pageable pageable) {

        return ResponseEntity.ok(sectionService.fetchAll(spec, pageable));
    }
}
