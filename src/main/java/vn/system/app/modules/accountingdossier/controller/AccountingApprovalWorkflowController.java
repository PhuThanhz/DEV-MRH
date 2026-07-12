package vn.system.app.modules.accountingdossier.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.accountingdossier.domain.request.AccountingApprovalWorkflowTemplateRequest;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalPreviewDTO;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalWorkflowTemplateDTO;
import vn.system.app.modules.accountingdossier.service.AccountingApprovalWorkflowService;

@RestController
@RequestMapping("/api/v1/accounting-approval-workflows")
@RequiredArgsConstructor
public class AccountingApprovalWorkflowController {

    private final AccountingApprovalWorkflowService service;

    @GetMapping
    @ApiMessage("Danh sách luồng duyệt chứng từ kế toán")
    public ResponseEntity<List<ResAccountingApprovalWorkflowTemplateDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PostMapping
    @ApiMessage("Tạo nháp luồng duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalWorkflowTemplateDTO> create(
            @Valid @RequestBody AccountingApprovalWorkflowTemplateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}/draft")
    @ApiMessage("Cập nhật nháp luồng duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalWorkflowTemplateDTO> updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody AccountingApprovalWorkflowTemplateRequest req) {
        return ResponseEntity.ok(service.updateDraft(id, req));
    }

    @PostMapping("/{id}/validate")
    @ApiMessage("Kiểm tra cấu hình luồng duyệt chứng từ kế toán")
    public ResponseEntity<List<String>> validate(@PathVariable Long id) {
        return ResponseEntity.ok(service.validate(id));
    }

    @PostMapping("/{id}/publish")
    @ApiMessage("Kích hoạt luồng duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalWorkflowTemplateDTO> publish(@PathVariable Long id) {
        return ResponseEntity.ok(service.publish(id));
    }

    @PostMapping("/{id}/deactivate")
    @ApiMessage("Ngưng hiệu lực luồng duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalWorkflowTemplateDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @PostMapping("/{id}/reactivate")
    @ApiMessage("Kích hoạt lại luồng duyệt chứng từ kế toán")
    public ResponseEntity<ResAccountingApprovalWorkflowTemplateDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivate(id));
    }

    @PostMapping("/{id}/copy")
    @ApiMessage("Sao chép luồng duyệt chứng từ kế toán thành bản nháp")
    public ResponseEntity<ResAccountingApprovalWorkflowTemplateDTO> copyToDraft(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.copyToDraft(id));
    }

    @PostMapping("/dossiers/{dossierId}/preview")
    @ApiMessage("Xem trước luồng duyệt của bộ chứng từ")
    public ResponseEntity<ResAccountingApprovalPreviewDTO> preview(@PathVariable Long dossierId) {
        return ResponseEntity.ok(service.preview(dossierId));
    }
}
