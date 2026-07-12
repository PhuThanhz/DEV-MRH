package vn.system.app.modules.accountingdossier.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import vn.system.app.common.response.ResultPaginationDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.accountingdossier.domain.request.AccountingApprovalDelegationRequest;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalDelegationDTO;
import vn.system.app.modules.accountingdossier.service.AccountingApprovalDelegationService;

@RestController
@RequestMapping("/api/v1/accounting-approval-delegations")
@RequiredArgsConstructor
public class AccountingApprovalDelegationController {

    private final AccountingApprovalDelegationService service;

    @GetMapping
    @ApiMessage("Danh sách ủy quyền duyệt chứng từ kế toán")
    public ResponseEntity<ResultPaginationDTO> list(Pageable pageable,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.list(pageable, keyword, status));
    }

    @PostMapping
    @ApiMessage("Tạo ủy quyền duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalDelegationDTO> create(
            @Valid @RequestBody AccountingApprovalDelegationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PostMapping("/{id}/activate")
    @ApiMessage("Kích hoạt ủy quyền duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalDelegationDTO> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @PostMapping("/{id}/revoke")
    @ApiMessage("Thu hồi ủy quyền duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalDelegationDTO> revoke(@PathVariable Long id) {
        return ResponseEntity.ok(service.revoke(id));
    }
}
