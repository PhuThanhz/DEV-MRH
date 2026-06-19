package vn.system.app.modules.document.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;
import vn.system.app.modules.document.domain.request.AccountingDocumentCategoryRequest;
import vn.system.app.modules.document.domain.response.ResAccountingDocumentCategoryDTO;
import vn.system.app.modules.document.repository.AccountingDocumentCategoryRepository;
import vn.system.app.modules.document.repository.DocumentRepository;

@Service
public class AccountingDocumentCategoryService {

    private final AccountingDocumentCategoryRepository repository;
    private final DocumentRepository documentRepository;

    public AccountingDocumentCategoryService(
            AccountingDocumentCategoryRepository repository,
            DocumentRepository documentRepository) {
        this.repository = repository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public ResAccountingDocumentCategoryDTO create(AccountingDocumentCategoryRequest req) {
        String code = req.getCategoryCode().trim().toUpperCase();
        if (repository.existsByCategoryCode(code)) {
            throw new IdInvalidException("Mã loại chứng từ kế toán đã tồn tại: " + code);
        }

        AccountingDocumentCategory entity = new AccountingDocumentCategory();
        applyRequest(entity, req, code);
        return convertToDTO(repository.save(entity));
    }

    @Transactional
    public ResAccountingDocumentCategoryDTO update(Long id, AccountingDocumentCategoryRequest req) {
        AccountingDocumentCategory current = fetchById(id);
        String code = req.getCategoryCode().trim().toUpperCase();
        if (repository.existsByCategoryCodeAndIdNot(code, id)) {
            throw new IdInvalidException("Mã loại chứng từ kế toán đã tồn tại: " + code);
        }

        applyRequest(current, req, code);
        return convertToDTO(repository.save(current));
    }

    @Transactional
    public void toggleActive(Long id) {
        AccountingDocumentCategory current = fetchById(id);
        if (current.isActive() && documentRepository.existsByAccountingCategory_Id(id)) {
            throw new IdInvalidException("Không thể vô hiệu hóa loại chứng từ đang được sử dụng");
        }
        current.setActive(!current.isActive());
        repository.save(current);
    }

    @Transactional
    public void delete(Long id) {
        AccountingDocumentCategory current = fetchById(id);
        if (documentRepository.existsByAccountingCategory_Id(id)) {
            throw new IdInvalidException("Không thể xóa loại chứng từ đang được sử dụng");
        }
        repository.delete(current);
    }

    public AccountingDocumentCategory fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Loại chứng từ kế toán không tồn tại"));
    }

    public ResultPaginationDTO fetchAll(Specification<AccountingDocumentCategory> spec, Pageable pageable) {
        Page<AccountingDocumentCategory> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        return rs;
    }

    public List<ResAccountingDocumentCategoryDTO> fetchActive() {
        return repository.findByActiveTrueOrderByCategoryNameAsc()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ResAccountingDocumentCategoryDTO convertToDTO(AccountingDocumentCategory e) {
        ResAccountingDocumentCategoryDTO dto = new ResAccountingDocumentCategoryDTO();
        dto.setId(e.getId());
        dto.setCategoryCode(e.getCategoryCode());
        dto.setCategoryName(e.getCategoryName());
        dto.setSymbol(e.getSymbol());
        dto.setDescription(e.getDescription());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());
        return dto;
    }

    private void applyRequest(AccountingDocumentCategory entity, AccountingDocumentCategoryRequest req, String code) {
        entity.setCategoryCode(code);
        entity.setCategoryName(req.getCategoryName().trim());
        entity.setSymbol(req.getSymbol());
        entity.setDescription(req.getDescription());
        entity.setActive(req.isActive());
    }
}
