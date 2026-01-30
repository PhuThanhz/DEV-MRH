package vn.system.app.modules.permissioncategory.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;
import vn.system.app.modules.permissioncategory.domain.request.PermissionCategoryRequest;
import vn.system.app.modules.permissioncategory.domain.response.PermissionCategoryResponse;
import vn.system.app.modules.permissioncategory.repository.PermissionCategoryRepository;

@Service
public class PermissionCategoryService {

    private final PermissionCategoryRepository repository;

    public PermissionCategoryService(PermissionCategoryRepository repository) {
        this.repository = repository;
    }

    // ================= CREATE =================
    public PermissionCategory handleCreate(PermissionCategoryRequest req) {

        if (repository.existsByCode(req.getCode())) {
            throw new RuntimeException("Code danh mục đã tồn tại");
        }

        PermissionCategory category = new PermissionCategory();
        category.setCode(req.getCode());
        category.setName(req.getName());

        return repository.save(category);
    }

    // ================= DELETE =================
    public void handleDelete(long id) {
        repository.deleteById(id);
    }

    // ================= GET BY ID =================
    public PermissionCategory fetchById(long id) {
        Optional<PermissionCategory> optional = repository.findById(id);
        return optional.orElse(null);
    }

    // ================= GET ALL + PAGINATION =================
    public ResultPaginationDTO fetchAll(
            Specification<PermissionCategory> spec,
            Pageable pageable) {

        Page<PermissionCategory> page = repository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        rs.setMeta(meta);

        List<PermissionCategoryResponse> result = page.getContent()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        rs.setResult(result);
        return rs;
    }

    // ================= CONVERT =================
    public PermissionCategoryResponse convertToResponse(
            PermissionCategory category) {

        PermissionCategoryResponse res = new PermissionCategoryResponse();
        res.setId(category.getId());
        res.setCode(category.getCode());
        res.setName(category.getName());
        return res;
    }
}
