package vn.system.app.modules.position.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;

import vn.system.app.modules.position.domain.Position;
import vn.system.app.modules.position.domain.request.ReqPositionDTO;
import vn.system.app.modules.position.domain.response.ResPositionDTO;
import vn.system.app.modules.position.service.PositionService;

@RestController
@RequestMapping("/api/v1")
public class PositionController {

    private final PositionService service;

    public PositionController(PositionService service) {
        this.service = service;
    }

    @PostMapping("/positions")
    @ApiMessage("Tạo vị trí trong tổ chức")
    public ResponseEntity<ResPositionDTO> create(@Valid @RequestBody ReqPositionDTO req)
            throws IdInvalidException {

        Position p = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.convert(p));
    }

    @GetMapping("/positions/{id}")
    @ApiMessage("Chi tiết vị trí")
    public ResponseEntity<ResPositionDTO> getOne(@PathVariable Long id)
            throws IdInvalidException {

        Position p = service.fetchEntity(id);
        return ResponseEntity.ok(service.convert(p));
    }
}