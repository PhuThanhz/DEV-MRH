package vn.system.app.modules.permissioncategory.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;
import vn.system.app.modules.permissioncategory.domain.request.PermissionCategoryRequest;
import vn.system.app.modules.permissioncategory.service.PermissionCategoryService;

@RestController
@RequestMapping("/api/v1/permission-categories")
public class PermissionCategoryController {

    private final PermissionCategoryService service;

    public PermissionCategoryController(PermissionCategoryService service) {
        this.service = service;
    }

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<PermissionCategory> create(
            @Valid @RequestBody PermissionCategoryRequest req) {

        PermissionCategory category = service.handleCreate(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(category);
    }

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<PermissionCategory> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                service.fetchAll(spec, pageable));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id) {

        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }
}
