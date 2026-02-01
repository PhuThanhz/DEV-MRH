package vn.system.app.modules.positionlevel.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.positionlevel.domain.request.*;
import vn.system.app.modules.positionlevel.domain.response.*;
import vn.system.app.modules.positionlevel.service.PositionLevelService;

@RestController
@RequestMapping("/api/v1/position-levels")
public class PositionLevelController {

    private final PositionLevelService service;

    public PositionLevelController(PositionLevelService service) {
        this.service = service;
    }

    // -------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------
    @PostMapping
    @ApiMessage("Tạo bậc chức danh")
    public ResponseEntity<ResCreatePositionLevelDTO> create(
            @Valid @RequestBody ReqCreatePositionLevelDTO req) {

        ResCreatePositionLevelDTO res = service.handleCreate(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // -------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------
    @PutMapping
    @ApiMessage("Cập nhật bậc chức danh")
    public ResponseEntity<ResPositionLevelDTO> update(
            @Valid @RequestBody ReqUpdatePositionLevelDTO req) {

        ResPositionLevelDTO res = service.handleUpdate(req);
        return ResponseEntity.ok(res);
    }

    // -------------------------------------------------------------
    // INACTIVE (soft delete)
    // -------------------------------------------------------------
    @DeleteMapping("/{id}")
    @ApiMessage("Ngừng kích hoạt bậc chức danh")
    public ResponseEntity<Void> inactive(@PathVariable Long id) {

        if (!service.existsById(id)) {
            throw new IdInvalidException("Bậc chức danh với id = " + id + " không tồn tại");
        }

        service.inactive(id); // ⭐ Soft delete = status = 0
        return ResponseEntity.ok(null);
    }

    // -------------------------------------------------------------
    // ACTIVE
    // -------------------------------------------------------------
    @PutMapping("/{id}/active")
    @ApiMessage("Kích hoạt lại bậc chức danh")
    public ResponseEntity<Void> active(@PathVariable Long id) {

        if (!service.existsById(id)) {
            throw new IdInvalidException("Bậc chức danh với id = " + id + " không tồn tại");
        }

        service.active(id); // ⭐ Set status = 1
        return ResponseEntity.ok(null);
    }

    // -------------------------------------------------------------
    // GET ALL
    // -------------------------------------------------------------
    @GetMapping
    @ApiMessage("Danh sách bậc chức danh")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<?> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll((Specification) spec, pageable));
    }

    // -------------------------------------------------------------
    // GET ONE
    // -------------------------------------------------------------
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết bậc chức danh")
    public ResponseEntity<ResPositionLevelDTO> getOne(@PathVariable Long id) {

        ResPositionLevelDTO res = service.fetchById(id);
        if (res == null) {
            throw new IdInvalidException("Bậc chức danh với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(res);
    }
}
