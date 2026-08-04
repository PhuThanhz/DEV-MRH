package vn.system.app.modules.approvaldelegation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.approvaldelegation.domain.request.ReqCreateApprovalDelegationDTO;
import vn.system.app.modules.approvaldelegation.domain.response.ResApprovalDelegationDTO;
import vn.system.app.modules.approvaldelegation.service.ApprovalDelegationService;

@RestController
@RequestMapping("/api/v1")
public class ApprovalDelegationController {

    private final ApprovalDelegationService delegationService;

    public ApprovalDelegationController(ApprovalDelegationService delegationService) {
        this.delegationService = delegationService;
    }

    @PostMapping("/approval-delegations")
    @ApiMessage("Tạo mới ủy quyền duyệt thành công")
    public ResponseEntity<ResApprovalDelegationDTO> create(
            @Valid @RequestBody ReqCreateApprovalDelegationDTO req) {

        ResApprovalDelegationDTO dto = delegationService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/approval-delegations/mine")
    @ApiMessage("Danh sách ủy quyền của tôi")
    public ResponseEntity<List<ResApprovalDelegationDTO>> fetchMyDelegations() {
        return ResponseEntity.ok(delegationService.fetchMyDelegations());
    }

    @GetMapping("/approval-delegations/delegated-to-me")
    @ApiMessage("Danh sách ủy quyền được giao cho tôi")
    public ResponseEntity<List<ResApprovalDelegationDTO>> fetchDelegationsToMe() {
        return ResponseEntity.ok(delegationService.fetchDelegationsToMe());
    }

    @PostMapping("/approval-delegations/{id}/revoke")
    @ApiMessage("Thu hồi ủy quyền duyệt thành công")
    public ResponseEntity<Void> revoke(@PathVariable("id") Long id) {
        delegationService.revoke(id);
        return ResponseEntity.ok().build();
    }
}
