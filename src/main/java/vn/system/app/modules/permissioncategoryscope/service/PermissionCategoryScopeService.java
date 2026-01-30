package vn.system.app.modules.permissioncategoryscope.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.department.service.DepartmentService;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.departmentjobtitle.service.DepartmentJobTitleService;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;
import vn.system.app.modules.permissioncategory.repository.PermissionCategoryRepository;
import vn.system.app.modules.permissioncategoryscope.domain.PermissionCategoryScope;
import vn.system.app.modules.permissioncategoryscope.domain.response.ResPermissionCategoryScopeGroupedDTO;
import vn.system.app.modules.permissioncategoryscope.repository.PermissionCategoryScopeRepository;

@Service
public class PermissionCategoryScopeService {

    private final PermissionCategoryScopeRepository repository;
    private final PermissionCategoryRepository categoryRepository;
    private final DepartmentJobTitleService departmentJobTitleService;
    private final DepartmentService departmentService;

    public PermissionCategoryScopeService(
            PermissionCategoryScopeRepository repository,
            PermissionCategoryRepository categoryRepository,
            DepartmentJobTitleService departmentJobTitleService,
            DepartmentService departmentService) {

        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.departmentJobTitleService = departmentJobTitleService;
        this.departmentService = departmentService;
    }

    /*
     * ==================================================
     * CREATE / UPDATE – UPSERT
     * ==================================================
     */
    @Transactional
    public ResPermissionCategoryScopeGroupedDTO createOrUpdate(
            Long categoryId,
            Long departmentId,
            List<Long> departmentJobTitleIds) {

        PermissionCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IdInvalidException("Danh mục quyền không tồn tại"));

        departmentService.fetchEntityById(departmentId);

        if (departmentJobTitleIds == null || departmentJobTitleIds.isEmpty()) {
            throw new IdInvalidException("Danh sách chức danh phòng ban không được rỗng");
        }

        Set<Long> uniqueIds = departmentJobTitleIds.stream().collect(Collectors.toSet());
        if (uniqueIds.size() != departmentJobTitleIds.size()) {
            throw new IdInvalidException("Danh sách chức danh phòng ban bị trùng");
        }

        // ===== validate & chống gán sai phòng ban =====
        for (Long djtId : uniqueIds) {
            DepartmentJobTitle djt = departmentJobTitleService.fetchEntityById(djtId);
            if (!djt.getDepartment().getId().equals(departmentId)) {
                throw new IdInvalidException("Chức danh không thuộc phòng ban đã chọn");
            }

            if (repository.existsByCategory_IdAndDepartmentJobTitleId(categoryId, djtId)) {
                throw new IdInvalidException("Chức danh đã được gán quyền");
            }
        }

        // ===== xoá cấu hình cũ của category trong phòng ban =====
        List<Long> allDeptJobTitleIds = departmentJobTitleService.fetchIdsByDepartment(departmentId);

        repository.deleteByCategory_IdAndDepartmentJobTitleIdIn(
                categoryId,
                allDeptJobTitleIds);

        // ===== insert mới =====
        for (Long djtId : uniqueIds) {
            PermissionCategoryScope scope = new PermissionCategoryScope();
            scope.setCategory(category);
            scope.setDepartmentJobTitleId(djtId);
            repository.save(scope);
        }

        return fetchGrouped(categoryId, departmentId);
    }

    /*
     * ==================================================
     * GET GROUPED – LOAD CHO UI
     * ==================================================
     */
    @Transactional(readOnly = true)
    public ResPermissionCategoryScopeGroupedDTO fetchGrouped(
            Long categoryId,
            Long departmentId) {

        PermissionCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IdInvalidException("Danh mục quyền không tồn tại"));

        var depRes = departmentService.fetchDepartmentById(departmentId);

        List<Long> deptJobTitleIds = departmentJobTitleService.fetchIdsByDepartment(departmentId);

        List<PermissionCategoryScope> scopes = repository.findByCategory_IdAndDepartmentJobTitleIdIn(
                categoryId,
                deptJobTitleIds);

        ResPermissionCategoryScopeGroupedDTO res = new ResPermissionCategoryScopeGroupedDTO();

        // ===== CATEGORY =====
        ResPermissionCategoryScopeGroupedDTO.Category cat = new ResPermissionCategoryScopeGroupedDTO.Category();
        cat.setId(category.getId());
        cat.setCode(category.getCode());
        cat.setName(category.getName());
        res.setCategory(cat);

        // ===== DEPARTMENT =====
        ResPermissionCategoryScopeGroupedDTO.Department dep = new ResPermissionCategoryScopeGroupedDTO.Department();
        dep.setId(depRes.getId());
        dep.setName(depRes.getName());
        res.setDepartment(dep);

        // ===== JOB TITLES =====
        Map<Long, ResPermissionCategoryScopeGroupedDTO.JobTitle> jtMap = new LinkedHashMap<>();

        for (PermissionCategoryScope scope : scopes) {
            DepartmentJobTitle djt = departmentJobTitleService.fetchEntityById(
                    scope.getDepartmentJobTitleId());

            ResPermissionCategoryScopeGroupedDTO.JobTitle jt = new ResPermissionCategoryScopeGroupedDTO.JobTitle();

            jt.setDepartmentJobTitleId(djt.getId());
            jt.setJobTitleId(djt.getJobTitle().getId());
            jt.setJobTitleName(djt.getJobTitle().getNameVi());

            jtMap.put(jt.getDepartmentJobTitleId(), jt);
        }

        res.setJobTitles(jtMap.values().stream().toList());
        return res;
    }

    /*
     * ==================================================
     * DELETE
     * ==================================================
     */
    @Transactional
    public void delete(Long categoryId, Long departmentId) {

        List<Long> deptJobTitleIds = departmentJobTitleService.fetchIdsByDepartment(departmentId);

        repository.deleteByCategory_IdAndDepartmentJobTitleIdIn(
                categoryId,
                deptJobTitleIds);
    }
}
