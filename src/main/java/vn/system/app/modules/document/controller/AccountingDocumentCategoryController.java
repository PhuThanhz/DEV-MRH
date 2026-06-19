package vn.system.app.modules.document.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;
import vn.system.app.modules.document.domain.request.AccountingDocumentCategoryRequest;
import vn.system.app.modules.document.domain.response.ResAccountingDocumentCategoryDTO;
import vn.system.app.modules.document.service.AccountingDocumentCategoryService;

@RestController
@RequestMapping("/api/v1/accounting-document-categories")
@RequiredArgsConstructor
public class AccountingDocumentCategoryController {

    private final AccountingDocumentCategoryService service;

    @PostMapping
    @ApiMessage("Tạo loại chứng từ kế toán")
    public ResponseEntity<ResAccountingDocumentCategoryDTO> create(
            @Valid @RequestBody AccountingDocumentCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật loại chứng từ kế toán")
    public ResponseEntity<ResAccountingDocumentCategoryDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountingDocumentCategoryRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PutMapping("/{id}/active")
    @ApiMessage("Thay đổi trạng thái loại chứng từ kế toán")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.toggleActive(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @ApiMessage("Chi tiết loại chứng từ kế toán")
    public ResponseEntity<ResAccountingDocumentCategoryDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToDTO(service.fetchById(id)));
    }

    @GetMapping
    @ApiMessage("Danh sách loại chứng từ kế toán")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<AccountingDocumentCategory> spec,
            Pageable pageable) {
        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    @GetMapping("/active")
    @ApiMessage("Danh sách loại chứng từ kế toán đang hoạt động")
    public ResponseEntity<List<ResAccountingDocumentCategoryDTO>> getActive() {
        return ResponseEntity.ok(service.fetchActive());
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa loại chứng từ kế toán")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
