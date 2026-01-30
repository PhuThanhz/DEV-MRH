package vn.system.app.modules.processaction.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.modules.processaction.domain.ProcessAction;
import vn.system.app.modules.processaction.domain.request.ReqCreateProcessActionDTO;
import vn.system.app.modules.processaction.domain.request.ReqUpdateProcessActionDTO;
import vn.system.app.modules.processaction.service.ProcessActionService;

@RestController
@RequestMapping("/api/v1/process-actions")
public class ProcessActionController {

    private final ProcessActionService service;

    public ProcessActionController(ProcessActionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProcessAction> create(
            @Valid @RequestBody ReqCreateProcessActionDTO req) {

        ProcessAction action = new ProcessAction();
        action.setCode(req.getCode());
        action.setName(req.getName());
        action.setShortDescription(req.getShortDescription());
        action.setDescription(req.getDescription());
        action.setActive(req.isActive());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.handleCreate(action));
    }

    @PutMapping
    public ResponseEntity<ProcessAction> update(
            @Valid @RequestBody ReqUpdateProcessActionDTO req) {

        ProcessAction action = new ProcessAction();
        action.setId(req.getId());
        action.setName(req.getName());
        action.setShortDescription(req.getShortDescription());
        action.setDescription(req.getDescription());
        action.setActive(req.isActive());

        return ResponseEntity.ok(service.handleUpdate(action));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessAction> getById(@PathVariable long id) {
        return ResponseEntity.ok(service.fetchById(id));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<ProcessAction> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }
}
