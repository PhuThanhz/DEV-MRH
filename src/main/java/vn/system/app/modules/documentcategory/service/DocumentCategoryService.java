package vn.system.app.modules.documentcategory.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.document.repository.DocumentRepository;
import vn.system.app.modules.documentcategory.domain.DocumentCategory;
import vn.system.app.modules.documentcategory.domain.request.DocumentCategoryRequest;
import vn.system.app.modules.documentcategory.domain.response.ResDocumentCategoryDTO;
import vn.system.app.modules.documentcategory.repository.DocumentCategoryRepository;

@Service
public class DocumentCategoryService {

    private final DocumentCategoryRepository repository;
    private final DocumentRepository documentRepository;

    public DocumentCategoryService(
            DocumentCategoryRepository repository,
            DocumentRepository documentRepository) {
        this.repository = repository;
        this.documentRepository = documentRepository;
    }

    // =====================================================
    // CREATE
    // =====================================================
    @Transactional
    public ResDocumentCategoryDTO handleCreate(DocumentCategoryRequest req) {
        String code = req.getCategoryCode().trim().toUpperCase();

        if (repository.existsByCategoryCode(code)) {
            throw new IdInvalidException("Mã danh mục đã tồn tại: " + code);
        }

        DocumentCategory entity = new DocumentCategory();
        entity.setCategoryCode(code);
        entity.setCategoryName(req.getCategoryName().trim());
        entity.setSymbol(req.getSymbol());
        entity.setDefinition(req.getDefinition());
        entity.setActive(req.isActive());
        entity.setMappingProcedure(req.isMappingProcedure());

        return convertToDTO(repository.save(entity));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @Transactional
    public ResDocumentCategoryDTO handleUpdate(Long id, DocumentCategoryRequest req) {
        DocumentCategory current = fetchById(id);
        String code = req.getCategoryCode().trim().toUpperCase();

        if (repository.existsByCategoryCodeAndIdNot(code, id)) {
            throw new IdInvalidException("Mã danh mục đã tồn tại: " + code);
        }

        current.setCategoryCode(code);
        current.setCategoryName(req.getCategoryName().trim());
        current.setSymbol(req.getSymbol());
        current.setDefinition(req.getDefinition());
        current.setActive(req.isActive());
        current.setMappingProcedure(req.isMappingProcedure());

        return convertToDTO(repository.save(current));
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================
    @Transactional
    public void handleToggleActive(Long id) {
        DocumentCategory current = fetchById(id);

        // Nếu đang active mà muốn deactivate -> check xem có văn bản nào đang dùng không
        if (current.isActive()) {
            boolean hasDocs = documentRepository.existsByCategory_Id(id);
            if (hasDocs) {
                throw new IdInvalidException("Không thể vô hiệu hóa danh mục này vì đang có văn bản sử dụng");
            }
        }

        current.setActive(!current.isActive());
        repository.save(current);
    }

    // =====================================================
    // DELETE
    // =====================================================
    @Transactional
    public void handleDelete(Long id) {
        DocumentCategory current = fetchById(id);

        boolean hasDocs = documentRepository.existsByCategory_Id(id);
        if (hasDocs) {
            throw new IdInvalidException("Không thể xóa danh mục này vì đang có văn bản sử dụng");
        }

        repository.delete(current);
    }

    // =====================================================
    // FETCH ONE
    // =====================================================
    public DocumentCategory fetchById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Danh mục loại văn bản không tồn tại"));
    }

    // =====================================================
    // FETCH ALL (paginated)
    // =====================================================
    public ResultPaginationDTO fetchAll(Specification<DocumentCategory> spec, Pageable pageable) {
        Page<DocumentCategory> page = repository.findAll(spec, pageable);

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

    // =====================================================
    // FETCH ALL ACTIVE
    // =====================================================
    public List<ResDocumentCategoryDTO> fetchAllActive() {
        return repository.findByActiveTrue()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // FETCH MAPPING PROCEDURE
    // =====================================================
    public List<ResDocumentCategoryDTO> fetchMappingProcedure() {
        return repository.findByMappingProcedureTrue()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // =====================================================
    // CONVERT TO DTO
    // =====================================================
    public ResDocumentCategoryDTO convertToDTO(DocumentCategory e) {
        ResDocumentCategoryDTO dto = new ResDocumentCategoryDTO();
        dto.setId(e.getId());
        dto.setCategoryCode(e.getCategoryCode());
        dto.setCategoryName(e.getCategoryName());
        dto.setSymbol(e.getSymbol());
        dto.setDefinition(e.getDefinition());
        dto.setActive(e.isActive());
        dto.setMappingProcedure(e.isMappingProcedure());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setUpdatedBy(e.getUpdatedBy());
        return dto;
    }
}