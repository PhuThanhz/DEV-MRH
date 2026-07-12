package vn.system.app.modules.accountingdossier.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalSlaSummaryDTO;
import vn.system.app.modules.accountingdossier.service.AccountingApprovalSlaService;

@RestController
@RequestMapping("/api/v1/accounting-approval-sla")
@RequiredArgsConstructor
public class AccountingApprovalSlaController {

    private final AccountingApprovalSlaService service;

    @PostMapping("/scan-overdue")
    @ApiMessage("Quét các bước duyệt bộ chứng từ quá hạn SLA")
    public ResponseEntity<ResAccountingApprovalSlaSummaryDTO> scanOverdue() {
        return ResponseEntity.ok(service.scanOverdueSteps());
    }
}
