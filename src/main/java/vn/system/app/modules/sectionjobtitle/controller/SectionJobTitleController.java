package vn.system.app.modules.sectionjobtitle.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;
import vn.system.app.modules.sectionjobtitle.domain.request.ReqSectionJobTitleDTO;
import vn.system.app.modules.sectionjobtitle.domain.response.ResSectionJobTitleDTO;
import vn.system.app.modules.sectionjobtitle.service.SectionJobTitleService;

@RestController
@RequestMapping("/api/v1/section-job-titles")
public class SectionJobTitleController {

    private final SectionJobTitleService service;

    public SectionJobTitleController(SectionJobTitleService service) {
        this.service = service;
    }

    @PostMapping
    @ApiMessage("Gán chức danh vào bộ phận (tự động khôi phục nếu đã hủy)")
    public ResponseEntity<ResSectionJobTitleDTO> create(
            @Valid @RequestBody ReqSectionJobTitleDTO req)
            throws IdInvalidException {

        SectionJobTitle entity = service.handleCreate(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.convertToResDTO(entity));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Hủy gán chức danh khỏi bộ phận (deactivate)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id)
            throws IdInvalidException {

        service.handleSoftDelete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/restore")
    @ApiMessage("Khôi phục gán chức danh vào bộ phận")
    public ResponseEntity<ResSectionJobTitleDTO> restore(@PathVariable Long id)
            throws IdInvalidException {

        SectionJobTitle entity = service.restore(id);
        return ResponseEntity.ok(service.convertToResDTO(entity));
    }

    @GetMapping("/{id}")
    @ApiMessage("Chi tiết gán chức danh - bộ phận")
    public ResponseEntity<ResSectionJobTitleDTO> getOne(@PathVariable Long id)
            throws IdInvalidException {

        SectionJobTitle entity = service.fetchEntityById(id);
        return ResponseEntity.ok(service.convertToResDTO(entity));
    }

    @GetMapping
    @ApiMessage("Danh sách gán chức danh bộ phận (phân trang + filter)")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<SectionJobTitle> spec,
            Pageable pageable) {

        // Fix lỗi: nếu không có sort thì áp dụng sort mặc định
        if (pageable.getSort().isEmpty()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("jobTitle.nameVi").ascending());
        }

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    @GetMapping("/sections/{sectionId}/job-titles")
    @ApiMessage("Danh sách chức danh đang hoạt động trong bộ phận")
    public ResponseEntity<List<ResSectionJobTitleDTO>> getActiveBySection(
            @PathVariable Long sectionId) {

        List<SectionJobTitle> list = service.fetchActiveBySection(sectionId);

        return ResponseEntity.ok(
                list.stream()
                        .map(service::convertToResDTO)
                        .collect(Collectors.toList()));
    }
}