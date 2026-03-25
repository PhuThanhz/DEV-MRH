package vn.system.app.modules.jobpositionnode.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jobpositionnode.domain.JobPositionNode;
import vn.system.app.modules.jobpositionnode.domain.request.ReqCreateNode;
import vn.system.app.modules.jobpositionnode.domain.request.ReqUpdateNode;
import vn.system.app.modules.jobpositionnode.domain.response.ResJobPositionNodeDTO;
import vn.system.app.modules.jobpositionnode.service.JobPositionNodeService;

@RestController
@RequestMapping("/api/v1")
public class JobPositionNodeController {

    private final JobPositionNodeService nodeService;

    public JobPositionNodeController(JobPositionNodeService nodeService) {
        this.nodeService = nodeService;
    }

    /*
     * ==================================
     * CREATE NODE
     * ==================================
     */
    @PostMapping("/job-position-nodes")
    @ApiMessage("Create job position node")
    public ResponseEntity<ResJobPositionNodeDTO> createNode(
            @RequestBody ReqCreateNode req) {

        JobPositionNode node = this.nodeService.handleCreateNode(req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.nodeService.convertToDTO(node));
    }

    /*
     * ==================================
     * DELETE NODE
     * ==================================
     */
    @DeleteMapping("/job-position-nodes/{id}")
    @ApiMessage("Delete job position node")
    public ResponseEntity<Void> deleteNode(@PathVariable Long id)
            throws IdInvalidException {

        JobPositionNode current = this.nodeService.fetchNodeById(id);

        if (current == null) {
            throw new IdInvalidException("Node với id = " + id + " không tồn tại");
        }

        this.nodeService.handleDeleteNode(id);

        return ResponseEntity.ok(null);
    }

    /*
     * ==================================
     * UPDATE NODE
     * ==================================
     */
    @PutMapping("/job-position-nodes")
    @ApiMessage("Update job position node")
    public ResponseEntity<ResJobPositionNodeDTO> updateNode(
            @RequestBody ReqUpdateNode req)
            throws IdInvalidException {

        JobPositionNode updated = this.nodeService.handleUpdateNode(req);

        if (updated == null) {
            throw new IdInvalidException("Node với id = " + req.getId() + " không tồn tại");
        }

        return ResponseEntity.ok(this.nodeService.convertToDTO(updated));
    }

    /*
     * ==================================
     * GET NODES BY CHART
     * ==================================
     */
    @GetMapping("/job-position-nodes/chart/{chartId}")
    @ApiMessage("Fetch nodes by chart")
    public ResponseEntity<List<ResJobPositionNodeDTO>> getNodesByChart(
            @PathVariable Long chartId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.nodeService.fetchNodesByChart(chartId));
    }
}