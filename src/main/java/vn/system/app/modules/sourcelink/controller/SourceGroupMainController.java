package vn.system.app.modules.sourcelink.controller;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceGroupMain;
import vn.system.app.modules.sourcelink.domain.request.ReqCreateGroupInMainDTO;
import vn.system.app.modules.sourcelink.domain.response.ResSourceGroupDTO;
import vn.system.app.modules.sourcelink.domain.response.ResSourceGroupMainDTO;
import vn.system.app.modules.sourcelink.service.SourceGroupMainService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/source-group-mains")
public class SourceGroupMainController {

    private final SourceGroupMainService mainService;

    public SourceGroupMainController(SourceGroupMainService mainService) {
        this.mainService = mainService;
    }

    // ============================================================
    // 1. TẠO NHÓM CHÍNH
    // ============================================================
    @PostMapping
    @ApiMessage("Tạo nhóm chính (SourceGroupMain)")
    public ResponseEntity<SourceGroupMain> create(@Valid @RequestBody SourceGroupMain req) {
        SourceGroupMain saved = mainService.handleCreate(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ============================================================
    // 2. TẠO NHÓM CON TRONG NHÓM CHÍNH
    // ============================================================
    @PostMapping("/{id}/groups")
    @ApiMessage("Tạo nhóm con trong nhóm chính")
    public ResponseEntity<ResSourceGroupMainDTO> createGroupInMain(
            @PathVariable("id") Long mainId,
            @Valid @RequestBody ReqCreateGroupInMainDTO req) {
        SourceGroup created = mainService.handleCreateGroupInMain(mainId, req.getGroupName());

        ResSourceGroupMainDTO dto = new ResSourceGroupMainDTO();
        dto.setId(created.getId());
        dto.setName(created.getName());
        dto.setCreatedAt(created.getCreatedAt());
        dto.setUpdatedAt(created.getUpdatedAt());
        dto.setTotalGroups(0);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // ============================================================
    // 3. LẤY DANH SÁCH NHÓM CON THEO NHÓM CHÍNH (TRẢ VỀ DTO)
    // ============================================================
    @GetMapping("/{id}/groups")
    @ApiMessage("Lấy danh sách nhóm con trong nhóm chính (có tổng số link)")
    public ResponseEntity<List<ResSourceGroupDTO>> getGroupsByMainId(@PathVariable("id") Long id) {
        List<SourceGroup> groups = mainService.handleGetGroupsByMainId(id);

        // Convert sang DTO
        List<ResSourceGroupDTO> dtoList = groups.stream().map(g -> {
            ResSourceGroupDTO dto = new ResSourceGroupDTO();
            dto.setId(g.getId());
            dto.setName(g.getName());
            dto.setCreatedAt(g.getCreatedAt());
            dto.setUpdatedAt(g.getUpdatedAt());
            dto.setTotalLinks(g.getLinks() != null ? g.getLinks().size() : 0);
            dto.setMainGroupId(id);
            dto.setMainGroupName(g.getMainGroup() != null ? g.getMainGroup().getName() : null);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    // ============================================================
    // 4. CẬP NHẬT NHÓM CHÍNH
    // ============================================================
    @PutMapping
    @ApiMessage("Cập nhật thông tin nhóm chính")
    public ResponseEntity<SourceGroupMain> update(@Valid @RequestBody SourceGroupMain req) {
        SourceGroupMain updated = mainService.handleUpdate(req);
        return ResponseEntity.ok(updated);
    }

    // ============================================================
    // 5. XÓA NHÓM CHÍNH
    // ============================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa nhóm chính (và toàn bộ nhóm con + link bên trong)")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        mainService.handleDelete(id);
        return ResponseEntity.ok(null);
    }

    // ============================================================
    // 6. DANH SÁCH TẤT CẢ NHÓM CHÍNH (PHÂN TRANG + FILTER)
    // ============================================================
    @GetMapping
    @ApiMessage("Lấy danh sách nhóm chính (có tổng số nhóm con)")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<SourceGroupMain> spec,
            Pageable pageable) {
        return ResponseEntity.ok(mainService.handleGetAll(spec, pageable));
    }
}
