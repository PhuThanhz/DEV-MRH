package vn.system.app.modules.permissioncontent.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.service.DepartmentJobTitleService;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;
import vn.system.app.modules.permissioncategory.repository.PermissionCategoryRepository;
import vn.system.app.modules.permissioncategoryscope.domain.PermissionCategoryScope;
import vn.system.app.modules.permissioncategoryscope.repository.PermissionCategoryScopeRepository;
import vn.system.app.modules.permissioncontent.domain.PermissionContent;
import vn.system.app.modules.permissioncontent.domain.request.ReqCreatePermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.request.ReqUpdatePermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.response.ResPermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.response.ResPermissionContentWithScopeDTO;
import vn.system.app.modules.permissioncontent.repository.PermissionContentRepository;

@Service
public class PermissionContentService {

    private final PermissionContentRepository repository;
    private final PermissionCategoryRepository categoryRepository;
    private final PermissionCategoryScopeRepository scopeRepository;
    private final DepartmentJobTitleService departmentJobTitleService;

    public PermissionContentService(
            PermissionContentRepository repository,
            PermissionCategoryRepository categoryRepository,
            PermissionCategoryScopeRepository scopeRepository,
            DepartmentJobTitleService departmentJobTitleService) {

        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.scopeRepository = scopeRepository;
        this.departmentJobTitleService = departmentJobTitleService;
    }

    // ==================================================
    // FETCH ENTITY (DÙNG CHO MODULE KHÁC)
    // ==================================================
    public PermissionContent fetchEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdInvalidException(
                        "PermissionContent với id = " + id + " không tồn tại"));
    }

    // ==================================================
    // CREATE
    // ==================================================
    @Transactional
    public ResPermissionContentWithScopeDTO create(ReqCreatePermissionContentDTO req) {

        PermissionCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IdInvalidException("Danh mục không tồn tại"));

        PermissionContent entity = new PermissionContent();
        entity.setName(req.getName());
        entity.setCategory(category);

        repository.save(entity);
        return buildResponse(entity);
    }

    // ==================================================
    // GET LIST BY CATEGORY
    // ==================================================
    @Transactional(readOnly = true)
    public List<ResPermissionContentDTO> fetchByCategory(Long categoryId) {

        return repository.findByCategory_Id(categoryId)
                .stream()
                .map(item -> {
                    ResPermissionContentDTO dto = new ResPermissionContentDTO();
                    dto.setId(item.getId());
                    dto.setName(item.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ==================================================
    // GET DETAIL
    // ==================================================
    @Transactional(readOnly = true)
    public ResPermissionContentWithScopeDTO fetchDetail(Long id) {
        return buildResponse(fetchEntityById(id));
    }

    // ==================================================
    // UPDATE
    // ==================================================
    @Transactional
    public ResPermissionContentWithScopeDTO update(Long id, ReqUpdatePermissionContentDTO req) {

        PermissionContent entity = fetchEntityById(id);
        entity.setName(req.getName());

        repository.save(entity);
        return buildResponse(entity);
    }

    // ==================================================
    // DELETE
    // ==================================================
    @Transactional
    public void delete(Long id) {
        repository.delete(fetchEntityById(id));
    }

    // ==================================================
    // BUILD RESPONSE (DÙNG CHUNG)
    // ==================================================
    private ResPermissionContentWithScopeDTO buildResponse(PermissionContent entity) {

        ResPermissionContentWithScopeDTO res = new ResPermissionContentWithScopeDTO();

        // ===== CONTENT =====
        ResPermissionContentWithScopeDTO.PermissionContent pc = new ResPermissionContentWithScopeDTO.PermissionContent();
        pc.setId(entity.getId());
        pc.setName(entity.getName());

        ResPermissionContentWithScopeDTO.Category cat = new ResPermissionContentWithScopeDTO.Category();
        cat.setId(entity.getCategory().getId());
        cat.setCode(entity.getCategory().getCode());
        cat.setName(entity.getCategory().getName());
        pc.setCategory(cat);

        res.setPermissionContent(pc);

        // ===== LOAD SCOPE =====
        List<PermissionCategoryScope> scopes = scopeRepository.findByCategory_Id(entity.getCategory().getId());

        Map<Long, ResPermissionContentWithScopeDTO.DepartmentScope> depMap = new LinkedHashMap<>();

        for (PermissionCategoryScope scope : scopes) {

            DepartmentJobTitle djt = departmentJobTitleService.fetchEntityById(
                    scope.getDepartmentJobTitleId());

            Long depId = djt.getDepartment().getId();

            depMap.putIfAbsent(depId,
                    new ResPermissionContentWithScopeDTO.DepartmentScope());

            var dep = depMap.get(depId);
            dep.setDepartmentId(depId);
            dep.setDepartmentName(djt.getDepartment().getName());

            if (dep.getJobTitles() == null) {
                dep.setJobTitles(new ArrayList<>());
            }

            ResPermissionContentWithScopeDTO.JobTitleScope jt = new ResPermissionContentWithScopeDTO.JobTitleScope();

            jt.setDepartmentJobTitleId(djt.getId());
            jt.setJobTitleId(djt.getJobTitle().getId());
            jt.setJobTitleName(djt.getJobTitle().getNameVi());

            dep.getJobTitles().add(jt);
        }

        res.setAppliedScopes(new ArrayList<>(depMap.values()));
        return res;
    }
}
