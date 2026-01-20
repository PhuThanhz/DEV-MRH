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
    // DELETE (inactive)
    // -------------------------------------------------------------
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa bậc chức danh")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        boolean exists = service.existsById(id);
        if (!exists) {
            throw new IdInvalidException("Bậc chức danh với id = " + id + " không tồn tại");
        }

        service.handleDelete(id);
        return ResponseEntity.ok(null);
    }

    // -------------------------------------------------------------
    // GET ALL (có filter, giống UserController)
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
